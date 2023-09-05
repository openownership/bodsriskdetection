package org.bodsrisk.data.openownership

import co.elastic.clients.elasticsearch.ElasticsearchClient
import com.beust.klaxon.JsonObject
import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileImportTask
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.Unpack
import org.bodsrisk.data.pscRefSlug
import org.bodsrisk.elasticsearch.DocumentBatch
import org.bodsrisk.elasticsearch.ElasticsearchDocument
import org.bodsrisk.elasticsearch.delete
import org.bodsrisk.model.Address
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.repository.Repository
import org.kbods.rdf.BodsRdfConfig
import org.kbods.rdf.iri
import org.kbods.rdf.plugins.CompaniesHouseRefPlugin
import org.kbods.rdf.toRdf
import org.kbods.rdf.vocabulary.BodsSchemaVersion
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatements
import org.slf4j.LoggerFactory
import java.io.File

@Singleton
class OpenOwnershipDataset(
    private val rdfRepository: Repository,
    private val esClient: ElasticsearchClient
) : DataImporter() {

    override fun createImportTask(): FileImportTask {
        return importTask {
            source(FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.GZIP))
            withIndex(INDEX)
            beforeStart {
                BodsVocabulary.write(rdfRepository, BodsSchemaVersion.V_0_2_0)
            }
            importRdf { line ->
                val bodsStatement = BodsStatement(line)
                bodsStatement.toRdf(bodsRdfConfig)
                    .plus(riskDetectionData(bodsStatement))
            }
            index(INDEX) { line ->
                val bodsStatement = BodsStatement(line)
                val jsonString = bodsStatement.jsonString { st, json ->
                    json["allNames"] = st.allNames
                }
                // TODO: Review handling of replace
//                replacedStatements.addAll(bodsStatement.replacesStatements)
                ElasticsearchDocument(jsonString, bodsStatement.id)
            }
        }
    }

    private fun index(jsonlFile: File, docBatch: DocumentBatch<String>) {
        val replacedStatements = mutableListOf<String>()
        log.info("Importing BODS file $jsonlFile to Elasticsearch")
        jsonlFile.useBodsStatements { bodsStatements ->
            bodsStatements
                .forEach { bodsStatement ->
                    val jsonString = bodsStatement.jsonString { st, json ->
                        json["allNames"] = st.allNames
                    }
                    replacedStatements.addAll(bodsStatement.replacesStatements)
                    docBatch.add(jsonString, bodsStatement.id)
                }
        }
        log.info("Deleting ${replacedStatements.size} replaced statements")
        replacedStatements.forEach {
            esClient.delete(INDEX, it)
        }
    }


    private fun riskDetectionData(statement: BodsStatement): List<Statement> {
        val statements = mutableListOf<Statement>()
        val iri = statement.iri()

        // Converting GB COH PSC references like
        // /company/OC306781/persons-with-significant-control/individual/nUVcsN0q0EAzOlyeyP4ZEHEH14g
        // to
        // gb-coh-psc-oc306781-nuvcsn0q0eazolyeyp4zeheh14g
        // which can then link to OpenSanctions records using "referents"
        statement.pscRefs()
            .filter { it.isNotBlank() }
            .mapNotNull { it.pscRefSlug() }
            .forEach { pscRef ->
                statements.add(iri.sameAs(FTM.iri(pscRef)))
            }

        // Add registered addresses if present
        statements.addAll(registeredAddressStatements(statement))

        return statements
    }

    companion object {
        private val log = LoggerFactory.getLogger(OpenOwnershipDataset::class.java)

        const val INDEX = "open-ownership"

        private const val DOWNLOAD_URL =
            "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.latest.jsonl.gz"

        private val bodsRdfConfig = BodsRdfConfig(relationshipsOnly = true)
            .withPlugins(CompaniesHouseRefPlugin())


        internal fun registeredAddressStatements(statement: BodsStatement): MutableList<Statement> {
            val statements = mutableListOf<Statement>()
            statement.json.array<JsonObject>("addresses")
                ?.filter { it.string("type") == "registered" }
                ?.forEach { address ->
                    val full = address.string("address")!!
                    val country = address.string("country")
                    if (country != null) {
                        val fullAddress = Address.cleanFullAddress(full, country)
                        statements.addAll(BodsRisk.registeredAddress(statement.iri(), fullAddress))
                    }
                }
            return statements
        }
    }
}

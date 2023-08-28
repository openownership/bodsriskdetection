package org.bodsrisk.data.openownership

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import com.beust.klaxon.JsonObject
import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.ImportTask
import org.bodsrisk.data.importer.Unpack
import org.bodsrisk.data.pscRefSlug
import org.bodsrisk.elasticsearch.DocumentBatch
import org.bodsrisk.elasticsearch.delete
import org.bodsrisk.elasticsearch.indexExists
import org.bodsrisk.model.Address
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.Statement
import org.kbods.rdf.BodsRdfConfig
import org.kbods.rdf.iri
import org.kbods.rdf.plugins.CompaniesHouseRefPlugin
import org.kbods.rdf.vocabulary.BodsSchemaVersion
import org.kbods.rdf.vocabulary.BodsVocabulary
import org.kbods.rdf.write
import org.kbods.read.BodsStatement
import org.kbods.read.useBodsStatements
import org.rdf4k.StatementsBatch
import org.slf4j.LoggerFactory
import java.io.File

@Singleton
class OpenOwnershipDataset(
    private val esIndices: ElasticsearchIndicesClient,
    private val esClient: ElasticsearchClient
) : DataImporter {

    override val requiresImport: Boolean = !esIndices.indexExists(INDEX)

    override fun buildTask(task: ImportTask) {
        val source = FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.GZIP)
        task.source(source)
            .importRdf { jsonlFile, rdfBatch ->
                importRdf(jsonlFile, rdfBatch)
            }.index(INDEX) { jsonlFile, docBatch ->
                index(jsonlFile, docBatch)
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

    private fun importRdf(jsonlFile: File, rdfBatch: StatementsBatch) {
        log.info("Importing BODS file $jsonlFile to RDF")
        val config = BodsRdfConfig(relationshipsOnly = true)
            .withPlugins(CompaniesHouseRefPlugin())

        BodsVocabulary.write(rdfBatch, BodsSchemaVersion.V_0_2_0)
        jsonlFile.useBodsStatements { bodsStatements ->
            bodsStatements
                .forEach { bodsStatement ->
                    bodsStatement.write(rdfBatch, config)
                    rdfBatch.add(riskDetectionData(bodsStatement))
                }
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
        const val INDEX = "open-ownership"
        private const val DOWNLOAD_URL =
            "https://oo-register-production.s3-eu-west-1.amazonaws.com/public/exports/statements.latest.jsonl.gz"
        private val log = LoggerFactory.getLogger(OpenOwnershipDataset::class.java)

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
package org.bodsrisk.data.icij

import co.elastic.clients.elasticsearch.ElasticsearchClient
import io.slink.iso3166.Country
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVRecord
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileImportTask
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.Unpack
import org.bodsrisk.elasticsearch.ElasticsearchDocument
import org.bodsrisk.elasticsearch.findByIds
import org.bodsrisk.model.Address
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.UnknownEntity
import org.bodsrisk.model.risk.riskId
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.eclipse.rdf4j.model.IRI
import org.rdf4k.literal
import org.rdf4k.statement

@Singleton
class IcijDataset(
    private val esClient: ElasticsearchClient,
) : DataImporter() {

    override fun createImportTask(): FileImportTask<Unit> {
        return statelessTask {
            source(FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.ZIP))
            withIndex(INDEX)
            files("nodes-addresses.csv") {
                csvToRdf { csvRecord ->
                    val country = Country.byCode(csvRecord["country_codes"])
                    if (country != null) {
                        val addressIri = BodsRisk.addressEntity(csvRecord.nodeId)
                        val fullAddress = csvRecord["address"]
                        BodsRisk.addressStatements(
                            addressIri,
                            Address.cleanFullAddress(fullAddress, country.alpha2Code)
                        )
                    } else {
                        emptyList()
                    }
                }
            }

            files("relationships.csv") {
                csvToRdf { csvRecord ->
                    if (csvRecord["rel_type"] == "registered_address") {
                        val targetIri = BodsRisk.icijEntity(csvRecord["node_id_start"])
                        val addressIri = BodsRisk.addressEntity(csvRecord["node_id_end"])
                        listOf(statement(targetIri, BodsRisk.PROP_REG_ADDRESS, addressIri))
                    } else {
                        emptyList()
                    }
                }

            }

            files("nodes-entities.csv", "nodes-officers.csv") {
                csvToRdf { csvRecord ->
                    val targetIri = BodsRisk.icijEntity(csvRecord.nodeId)

                    // We only need the risks in RDF for now
                    listOf(
                        statement(targetIri, BodsRisk.PROP_HAS_RISK, "icij".literal()),
                        statement(targetIri, BodsRisk.PROP_HAS_RISK, csvRecord.sourceId.riskId().literal())
                    )
                }
                indexCsv(INDEX) { csvRecord ->

                    // We use UnknownEntity here because the ICIJ data isn't specific about the entity type
                    val entity = UnknownEntity(
                        iri = BodsRisk.icijEntity(csvRecord.nodeId),
                        name = csvRecord["name"],
                        source = DataSource.ICIJ
                    )
                    ElasticsearchDocument(entity, csvRecord.nodeId)
                }
            }
        }
    }

    fun getEntities(iris: Collection<IRI>): Map<String, Entity> {
        return esClient.findByIds<UnknownEntity>(INDEX, iris.map { it.localName })
    }

    companion object {
        val INDEX = "icij"
        private val DOWNLOAD_URL = "https://offshoreleaks-data.icij.org/offshoreleaks/csv/full-oldb.LATEST.zip"
    }
}

private val CSVRecord.nodeId: String get() = get("node_id")
private val CSVRecord.sourceId: String get() = get("sourceID")

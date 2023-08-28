package org.bodsrisk.data.icij

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import co.elastic.clients.json.JsonData
import io.slink.iso3166.Country
import jakarta.inject.Singleton
import jakarta.json.JsonObject
import org.apache.commons.csv.CSVRecord
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.ImportTask
import org.bodsrisk.data.importer.Unpack
import org.bodsrisk.elasticsearch.DocumentBatch
import org.bodsrisk.elasticsearch.findByIds
import org.bodsrisk.elasticsearch.indexExists
import org.bodsrisk.model.Address
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.UnknownEntity
import org.bodsrisk.model.risk.riskId
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.utils.forEachCsvRecord
import org.eclipse.rdf4j.model.IRI
import org.rdf4k.StatementsBatch
import org.rdf4k.iri
import org.rdf4k.literal
import org.rdf4k.statement
import java.io.File

@Singleton
class IcijDataset(
    private val esClient: ElasticsearchClient,
    private val esIndices: ElasticsearchIndicesClient,
) : DataImporter {

    override val requiresImport: Boolean = !esIndices.indexExists(INDEX)

    override fun buildTask(task: ImportTask) {
        val source = FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.ZIP)
        task.source(source)
            .importRdf { csv, rdfBatch ->
                when (csv.name) {
                    "nodes-addresses.csv" -> importAddresses(csv, rdfBatch)
                    "relationships.csv" -> importRegisteredAddresses(csv, rdfBatch)
                    in filesWithEntities -> importEntityRdf(csv, rdfBatch)
                }
            }
            .index(INDEX) { csv, docBatch ->
                if (csv.name in filesWithEntities) {
                    indexEntities(csv, docBatch)
                }
            }
    }

    private fun importEntityRdf(csv: File, rdfBatch: StatementsBatch) {
        csv.forEachCsvRecord { csvRecord ->
            val targetIri = BodsRisk.icijEntity(csvRecord.nodeId)
            // We only need the risks in RDF for now
            rdfBatch.add(targetIri, BodsRisk.PROP_HAS_RISK, "icij".literal())
            rdfBatch.add(targetIri, BodsRisk.PROP_HAS_RISK, csvRecord.sourceId.riskId().literal())
        }
    }

    private fun indexEntities(csv: File, docBatch: DocumentBatch<Entity>) {
        csv.forEachCsvRecord { csvRecord ->
            val entity = UnknownEntity(
                iri = BodsRisk.icijEntity(csvRecord.nodeId),
                name = csvRecord["name"],
                source = DataSource.ICIJ
            )
            docBatch.add(entity, csvRecord.nodeId)
        }
    }

    private fun importAddresses(csvFile: File, rdfBatch: StatementsBatch) {
        csvFile.forEachCsvRecord { csvRecord ->
            val country = Country.byCode(csvRecord["country_codes"])
            if (country != null) {
                val addressIri = BodsRisk.addressEntity(csvRecord.nodeId)
                val fullAddress = csvRecord["address"]
                val statements =
                    BodsRisk.addressStatements(addressIri, Address.cleanFullAddress(fullAddress, country.alpha2Code))
                rdfBatch.add(statements)
            }
        }
    }

    private fun importRegisteredAddresses(csvFile: File, rdfBatch: StatementsBatch) {
        csvFile.forEachCsvRecord { csvRecord ->
            // We're only interested in addresses for now
            if (csvRecord["rel_type"] == "registered_address") {
                val targetIri = BodsRisk.icijEntity(csvRecord["node_id_start"])
                val addressIri = BodsRisk.addressEntity(csvRecord["node_id_end"])
                rdfBatch.add(statement(targetIri, BodsRisk.PROP_REG_ADDRESS, addressIri))
            }
        }
    }

    fun getEntities(iris: Collection<IRI>): Map<String, Entity> {
        return esClient.findByIds<JsonData>(INDEX, iris.map { it.localName })
            .map { entry ->
                val json = entry.value.toJson() as JsonObject
                entry.key to UnknownEntity(
                    iri = json.getString("iri").iri(),
                    name = json.getString("name")!!,
                    source = DataSource.ICIJ
                )
            }.toMap()
    }

    companion object {
        val INDEX = "icij"
        private val DOWNLOAD_URL = "https://offshoreleaks-data.icij.org/offshoreleaks/csv/full-oldb.LATEST.zip"
        private val filesWithEntities = listOf("nodes-entities.csv", "nodes-officers.csv")
    }
}

private val CSVRecord.nodeId: String get() = get("node_id")
private val CSVRecord.sourceId: String get() = get("sourceID")

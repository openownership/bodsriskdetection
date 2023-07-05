package org.bodsrisk.data.opensanctions

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import co.elastic.clients.json.JsonData
import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.ImportTask
import org.bodsrisk.elasticsearch.indexExists
import org.bodsrisk.elasticsearch.terms
import org.bodsrisk.utils.toKlaxonJson

@Singleton
class OpenSanctionsDataset(
    private val esClient: ElasticsearchClient,
    private val esIndices: ElasticsearchIndicesClient,
) : DataImporter {

    private val source = FileSource.Remote(DOWNLOAD_URL)
    override val requiresImport: Boolean = !esIndices.indexExists(INDEX)

    override fun buildTask(task: ImportTask) {
        task.source(source)
            .importRdf { jsonlFile, rdfBatch ->
                jsonlFile.forEachLine { jsonString ->
                    rdfBatch.add(jsonString.toKlaxonJson().toRdf())
                }
            }
            .index(INDEX) { jsonlFile, docBatch ->
                jsonlFile.forEachLine { jsonString ->
                    docBatch.add(jsonString)
                }
            }
    }

    fun getRecord(idOrRef: String): JsonData? {
        return getRecords(listOf(idOrRef))
            .firstOrNull()
    }

    fun getRecords(idsOrRefs: List<String>): List<JsonData> {
        return esClient.search({ request ->
            request.index(INDEX)
                .query { query ->
                    query.bool { bool ->
                        bool.should { q ->
                            q.terms(FIELD_ID, idsOrRefs)
                        }.should { q ->
                            q.terms(FIELD_REFERENTS, idsOrRefs)
                        }
                    }
                }
        }, JsonData::class.java)
            .hits()
            .hits()
            .map { it.source()!! }
    }

    companion object {
        val INDEX = "open-sanctions"
        private val DOWNLOAD_URL = "https://data.opensanctions.org/datasets/latest/default/entities.ftm.json"
        private const val FIELD_ID = "id"
        private const val FIELD_REFERENTS = "referents"
    }
}

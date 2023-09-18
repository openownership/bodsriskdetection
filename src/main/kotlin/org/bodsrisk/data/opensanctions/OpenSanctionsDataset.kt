package org.bodsrisk.data.opensanctions

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.JsonData
import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileImportTask
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.StatelessTask
import org.bodsrisk.elasticsearch.ElasticsearchDocument
import org.bodsrisk.elasticsearch.terms
import org.bodsrisk.utils.toKlaxonJson

@Singleton
class OpenSanctionsDataset(
    private val esClient: ElasticsearchClient,
) : DataImporter() {

    override fun createImportTask(): StatelessTask {
        return statelessTask {
            source(FileSource.Remote(DOWNLOAD_URL))
            withIndex(INDEX)
            importRdf { it.toKlaxonJson().toRdf() }
            index(INDEX) { ElasticsearchDocument(it) }
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

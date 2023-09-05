package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient

class DocumentBatch<T>(
    private val elasticsearchClient: ElasticsearchClient,
    private val index: String,
    private val size: Int = 100
) : AutoCloseable {

    private val documents = mutableListOf<ElasticsearchDocument<T>>()

    fun add(document: T, id: String? = null) {
        val doc = ElasticsearchDocument(document, id)
        addDocument(doc)
    }

    fun addDocument(doc: ElasticsearchDocument<T>) {
        documents.add(doc)
        if (documents.size == size) {
            bulkIndex()
        }
    }

    fun add(documents: Collection<T>) {
        documents.forEach { add(it) }
    }

    override fun close() {
        if (documents.size > 0) {
            bulkIndex()
        }
    }

    private fun bulkIndex() {
        elasticsearchClient.indexDocs(index, documents)
        documents.clear()
    }
}

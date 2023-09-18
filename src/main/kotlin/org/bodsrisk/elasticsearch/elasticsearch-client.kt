package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder

fun <T> ElasticsearchClient.withBatch(index: String, batchSize: Int = 1000, block: (DocumentBatch<T>) -> Unit) {
    DocumentBatch<T>(this, index, batchSize).use(block)
}

inline fun <reified T> ElasticsearchClient.findByTerms(
    index: String,
    field: String,
    terms: Collection<String>
): List<Hit<T>> {
    return search({ request ->
        request.index(index)
            .size(terms.size)
            .query { q ->
                q.terms(field, terms)
            }
    }, T::class.java)
        .hits()
        .hits()
}

inline fun <reified T> ElasticsearchClient.findByIds(
    index: String,
    ids: Collection<String>
): Map<String, T> {
    return search({ request ->
        request.index(index)
            .size(ids.size)
            .query { q ->
                q.ids { idsQuery ->
                    idsQuery.values(ids.toList())
                }
            }
    }, T::class.java)
        .hits()
        .hits()
        .associate { it.id() to it.source()!! }
}

inline fun <reified T> ElasticsearchClient.get(index: String, id: String): T? {
    return get({
        it.index(index).id(id)
    }, T::class.java).source()
}

fun ElasticsearchClient.delete(index: String, id: String) {
    delete {
        it.index(index)
            .id(id)
    }
}

fun ElasticsearchClient.findOneJson(index: String, field: String, value: String): String? {
    return search({ request ->
        request.index(index)
            .query { q ->
                q.term(field, value)
            }

    }, JsonData::class.java)
        .hits()
        .hits()
        .firstOrNull()
        ?.source()
        ?.toString()
}

fun ElasticsearchClient.index(index: String, documents: List<String>) {
    bulk { bulkRequest ->
        bulkRequest.index(index)
        documents.forEach { doc ->
            bulkRequest.operations { operations ->
                operations.index { request ->
                    request.document(JsonData.fromJson(doc))
                }
            }
        }
        bulkRequest
    }.checkErrors()
}

fun ElasticsearchClient.indexDocs(index: String, documents: List<ElasticsearchDocument<*>>) {
    bulk { bulkRequest ->
        bulkRequest.index(index)
        documents.forEach { doc ->
            bulkRequest.operations { operations ->
                batchDocIndex(operations, doc)
            }
        }
        bulkRequest
    }.checkErrors()
}

private fun batchDocIndex(
    operations: BulkOperation.Builder,
    batchDoc: ElasticsearchDocument<*>
): ObjectBuilder<BulkOperation> {
    return when (batchDoc.document) {
        is String -> operations.index { request ->
            request.document(JsonData.fromJson(batchDoc.document))
                .id(batchDoc.id)
        }

        else -> operations.index { request ->
            request.document(batchDoc.document)
                .id(batchDoc.id)
        }
    }
}

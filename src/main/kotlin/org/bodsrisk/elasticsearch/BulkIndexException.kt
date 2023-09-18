package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.core.BulkResponse
import org.slf4j.LoggerFactory

class BulkIndexException(private val bulkResponse: BulkResponse) : RuntimeException(errorMessage(bulkResponse)) {

    companion object {
        private fun errorMessage(bulkResponse: BulkResponse): String {
            return "Elasticsearch bulk index error: " +
                    bulkResponse.items()
                        .filter { it.error() != null }
                        .joinToString(", ") { item ->
                            "${item.index()} / ${item.id()}: ${item.error()}"
                        }
        }
    }
}

private val log = LoggerFactory.getLogger("org.bodsrisk.elasticsearch")

internal fun BulkResponse.checkErrors() {
    if (errors()) {
        throw BulkIndexException(this)
    }
}
package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import io.slink.resources.resourceAsInput
import org.slf4j.LoggerFactory
import java.io.InputStream

private val log = LoggerFactory.getLogger("org.bodsrisk.elasticsearch")

fun ElasticsearchIndicesClient.indexTemplateExists(name: String): Boolean {
    return existsIndexTemplate { it.name(name) }.value()
}

fun ElasticsearchIndicesClient.newIndexTemplate(name: String, classpathJson: String): ElasticsearchIndicesClient {
    newIndexTemplate(name, resourceAsInput(classpathJson))
    return this
}

fun ElasticsearchIndicesClient.newIndexTemplate(name: String, content: InputStream): ElasticsearchIndicesClient {
    if (!indexTemplateExists(name)) {
        log.info("Elasticsearch template '$name' doesn't exist, creating")
        putIndexTemplate {
            it.name(name)
                .withJson(content)
        }
    } else {
        log.info("Elasticsearch template '$name' already exists")
    }
    return this
}

fun ElasticsearchIndicesClient.deleteIndexTemplate(name: String): ElasticsearchIndicesClient {
    if (indexTemplateExists(name)) {
        log.info("Deleting index template $name")
        deleteIndexTemplate {
            it.name(name)
        }
    }
    return this
}

fun ElasticsearchIndicesClient.indexExists(name: String): Boolean {
    return this.exists { it.index(name) }.value()
}

fun ElasticsearchIndicesClient.deleteIndex(name: String): ElasticsearchIndicesClient {
    if (indexExists(name)) {
        this.delete {
            log.info("Deleting index ${name}")
            it.index(name)
        }
    }
    return this
}

fun ElasticsearchIndicesClient.createIndex(name: String): ElasticsearchIndicesClient {
    if (!indexExists(name)) {
        log.info("Elasticsearch index '$name' doesn't exist, creating")
        create {
            it.index(name)
        }
    } else {
        log.info("Elasticsearch index '$name' already exists")
    }
    return this
}

fun ElasticsearchIndicesClient.wipeIndex(index: String) {
    deleteIndex(index)
    deleteIndexTemplate(index)
    newIndexTemplate(index, "elasticsearch/templates/$index.json")
}

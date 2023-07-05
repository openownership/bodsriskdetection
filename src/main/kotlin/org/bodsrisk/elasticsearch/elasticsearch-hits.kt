package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.json.JsonData
import jakarta.json.JsonObject

fun Hit<JsonData>.json(): JsonObject {
    return this.source()!!.toJson() as JsonObject
}

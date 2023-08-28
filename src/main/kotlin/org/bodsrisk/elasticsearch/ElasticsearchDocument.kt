package org.bodsrisk.elasticsearch

data class ElasticsearchDocument<T>(
    val document: T,
    val id: String? = null
)
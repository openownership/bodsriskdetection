package org.bodsrisk.elasticsearch

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.elasticsearch")
data class ElasticsearchConfig
@ConfigurationInject
constructor(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
)
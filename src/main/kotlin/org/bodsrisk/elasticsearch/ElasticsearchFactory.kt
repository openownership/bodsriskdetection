package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.ssl.SSLContexts
import org.elasticsearch.client.RestClient


@Factory
class ElasticsearchFactory(private val config: ElasticsearchConfig) {

    private val transport: ElasticsearchTransport = createTransport()

    @Singleton
    fun client(): ElasticsearchClient = ElasticsearchClient(transport)

    @Singleton
    fun indices(): ElasticsearchIndicesClient = ElasticsearchIndicesClient(transport)

    private fun createTransport(): RestClientTransport {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))
        val host = HttpHost(config.host, config.port, "https")
        val builder = RestClient.builder(host)
        builder.setHttpClientConfigCallback { clientBuilder ->
            clientBuilder.setDefaultCredentialsProvider(credentialsProvider)

            val sslBuilder = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
            val sslContext = sslBuilder.build()
            clientBuilder.setSSLContext(sslContext)
            clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)

            clientBuilder
        }
        builder.setNodeSelector { it.first() }

        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return RestClientTransport(builder.build(), JacksonJsonpMapper(objectMapper))
    }
}
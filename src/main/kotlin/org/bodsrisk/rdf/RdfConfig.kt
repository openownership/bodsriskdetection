package org.bodsrisk.rdf

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.manager.RepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import org.rdf4k.createIfNotPresent
import org.rdf4k.rio.resourceAsRdfModel
import org.slf4j.LoggerFactory

@ConfigurationProperties("app.rdf")
data class RdfConfig
@ConfigurationInject
constructor(
    val connectionUrl: String,
    val username: String,
    val password: String,
) {

    private val repositoryManager: RepositoryManager

    init {
        repositoryManager = RemoteRepositoryManager(connectionUrl)
        repositoryManager.setUsernameAndPassword(username, password)
        repositoryManager.init()
    }

    fun getRepository(repositoryId: String): Repository {
        createRepository(repositoryId)
        return repositoryManager.getRepository(repositoryId)
    }

    private fun createRepository(repositoryId: String) {
        if (repositoryManager.hasRepositoryConfig(repositoryId)) {
            log.info("Repository $repositoryId exists, skipping.")
            return
        }

        log.info("Creating RDF repository $repositoryId")
        val config = resourceAsRdfModel("graphdb/graphdb-repository.ttl")
        val created = repositoryManager.createIfNotPresent(repositoryId, config)
        if (created) {
            repositoryManager.getRepository(repositoryId).connection.use { connection ->
                connection.add(resourceAsRdfModel("graphdb/ftm.xml", RDFFormat.RDFXML))
                connection.add(resourceAsRdfModel("graphdb/bods-risk.ttl", RDFFormat.TURTLE))
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RdfConfig::class.java)
    }
}

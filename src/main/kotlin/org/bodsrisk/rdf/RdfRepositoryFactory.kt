package org.bodsrisk.rdf

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import org.eclipse.rdf4j.repository.Repository

@Factory
class RdfRepositoryFactory(private val config: RdfConfig) {

    @Singleton
    fun createRepository(): Repository {
        return config.getRepository(RdfRepositories.BODS)
    }
}

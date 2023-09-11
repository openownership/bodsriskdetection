package org.bodsrisk.service.entityresolver

import jakarta.inject.Singleton
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.graph.Relationship
import org.bodsrisk.model.toEntity
import org.bodsrisk.model.toRelationship
import org.bodsrisk.service.BodsService
import org.eclipse.rdf4j.model.IRI
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.iri
import org.rdf4k.iri
import org.rdf4k.toIri

@Singleton
class OpenOwnershipResolver(private val bodsService: BodsService) : DataSourceResolver {

    override val dataSource = DataSource.OpenOwnership

    override fun resolveEntity(iris: Collection<IRI>): Map<IRI, Entity> {
        return bodsService.getStatements(iris.map { it.localName })
            .map { entry ->
                val iri = BodsRdf.RESOURCE.iri(entry.key)
                iri to entry.value.toEntity(iri)
            }
            .toMap()
    }

    override fun resolveRelationships(iris: Collection<IRI>): Map<IRI, Relationship> {
        return bodsService.getStatements(iris.map { it.localName })
            .map { entry ->
                BodsRdf.RESOURCE.iri(entry.key) to entry.value.toRelationship()
            }
            .toMap()    }
}
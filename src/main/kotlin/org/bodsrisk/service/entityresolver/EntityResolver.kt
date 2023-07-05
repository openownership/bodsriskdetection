package org.bodsrisk.service.entityresolver

import jakarta.inject.Singleton
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.graph.Relationship
import org.eclipse.rdf4j.model.IRI

@Singleton
class EntityResolver(private val dataSourceResolvers: List<DataSourceResolver>) {

    private val resolversBySource = dataSourceResolvers.associateBy { it.dataSource }

    fun resolveEntity(iris: Collection<IRI>): Map<IRI, Entity> {
        val entities = mutableMapOf<IRI, Entity>()
        iris.groupBy { DataSource.forIri(it) }
            .filter { it.key != null }
            .forEach { (dataSource, sourceIris) ->
                val sourceEntities = resolversBySource[dataSource]!!.resolveEntity(sourceIris)
                entities.putAll(sourceEntities)
            }
        return entities
    }

    fun resolveRelationships(iris: Collection<IRI>): Map<IRI, Relationship> {
        val relationships = mutableMapOf<IRI, Relationship>()
        iris.groupBy { DataSource.forIri(it) }
            .filter { it.key != null }
            .forEach { (dataSource, sourceIris) ->
                val sourceEntities = resolversBySource[dataSource]!!.resolveRelationships(sourceIris)
                relationships.putAll(sourceEntities)
            }
        return relationships
    }

    fun resolveEntity(iri: IRI): Entity? {
        return resolveEntity(listOf(iri)).entries.firstOrNull()?.value
    }
}

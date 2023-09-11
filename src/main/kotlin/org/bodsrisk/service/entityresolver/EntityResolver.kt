package org.bodsrisk.service.entityresolver

import jakarta.inject.Singleton
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.graph.Relationship
import org.eclipse.rdf4j.model.IRI
import org.slf4j.LoggerFactory

@Singleton
class EntityResolver(private val dataSourceResolvers: List<DataSourceResolver>) {

    private val resolversBySource = dataSourceResolvers.associateBy { it.dataSource }

    fun resolveEntities(iris: Collection<IRI>): Map<IRI, Entity> {
        val entities = mutableMapOf<IRI, Entity>()
        iris.groupBy { DataSource.forIri(it) }
            .filter { it.key != null }
            .forEach { (dataSource, sourceIris) ->
                val sourceEntities = resolversBySource[dataSource]!!.resolveEntity(sourceIris)
                entities.putAll(sourceEntities)
            }
        if (entities.size < iris.size) {
            val unresolved = iris.filter { it !in entities.keys }
            log.warn("Could not resolve ${unresolved.size} of ${iris.size} entities: " + unresolved.joinToString(", "))
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
        if (relationships.size < iris.size) {
            val unresolved = iris.filter { it !in relationships.keys }
            log.warn("Could not resolve ${unresolved.size} of ${iris.size} relationships: " + unresolved.joinToString(", "))
        }
        return relationships
    }

    fun resolveEntities(iri: IRI): Entity? {
        return resolveEntities(listOf(iri)).entries.firstOrNull()?.value
    }

    companion object {
        private val log = LoggerFactory.getLogger(EntityResolver::class.java)
    }
}

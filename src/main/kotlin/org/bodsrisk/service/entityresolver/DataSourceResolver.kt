package org.bodsrisk.service.entityresolver

import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.graph.Relationship
import org.eclipse.rdf4j.model.IRI

interface DataSourceResolver {
    val dataSource: DataSource
    fun resolveEntity(iris: Collection<IRI>): Map<IRI, Entity>
    fun resolveRelationships(iris: Collection<IRI>): Map<IRI, Relationship>
}
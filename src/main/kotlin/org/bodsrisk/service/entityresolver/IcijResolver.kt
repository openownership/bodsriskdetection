package org.bodsrisk.service.entityresolver

import jakarta.inject.Singleton
import org.bodsrisk.data.icij.IcijDataset
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.graph.Relationship
import org.eclipse.rdf4j.model.IRI

@Singleton
class IcijResolver(private val icijDataset: IcijDataset) : DataSourceResolver {

    override val dataSource: DataSource = DataSource.ICIJ

    override fun resolveEntity(iris: Collection<IRI>): Map<IRI, Entity> {
        return icijDataset.getEntities(iris)
            .values
            .associateBy { it.iri }
    }

    override fun resolveRelationships(iris: Collection<IRI>): Map<IRI, Relationship> {
        return emptyMap()
    }
}

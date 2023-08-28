package org.bodsrisk.service.entityresolver

import jakarta.inject.Singleton
import org.bodsrisk.data.opensanctions.OpenSanctionsDataset
import org.bodsrisk.model.DataSource
import org.bodsrisk.model.Entity
import org.bodsrisk.model.fromOpenSanctionsRecord
import org.bodsrisk.model.graph.Relationship
import org.eclipse.rdf4j.model.IRI

@Singleton
class OpenSanctionsResolver(private val openSanctionsDataset: OpenSanctionsDataset) : DataSourceResolver {

    override val dataSource = DataSource.OpenSanctions

    override fun resolveEntity(iris: Collection<IRI>): Map<IRI, Entity> {
        return openSanctionsDataset.getRecords(iris.map { it.localName })
            .associate { record ->
                val entity = fromOpenSanctionsRecord(record)
                entity.iri to entity
            }
    }

    override fun resolveRelationships(iris: Collection<IRI>): Map<IRI, Relationship> {
        return emptyMap()
    }
}


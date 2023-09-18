package org.bodsrisk.service.network.explain

import jakarta.inject.Inject
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.service.entityresolver.EntityResolver
import org.bodsrisk.service.network.NetworkService
import org.bodsrisk.service.network.RelationshipExplanation
import org.eclipse.rdf4j.model.IRI

abstract class RelationshipExplainer(val relationshipType: RelatedEntityRelationship) {

    @Inject
    lateinit var networkService: NetworkService

    @Inject
    lateinit var entityResolver: EntityResolver

    abstract fun explain(target: IRI, relatedEntity: IRI, intermediateEntity: IRI?): RelationshipExplanation
}
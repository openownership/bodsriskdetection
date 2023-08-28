package org.bodsrisk.service.network

import jakarta.inject.Singleton
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.service.network.explain.RelationshipExplainer
import org.eclipse.rdf4j.model.IRI

/**
 *  Only exists to avoid cyclic dependency between RelationshipExplainer
 *  and NetworkService (which is where explainRelationship belongs)
 */
@Singleton
class RelationshipService(
    private val relationshipExplainers: List<RelationshipExplainer>
) {

    private val explainers = relationshipExplainers.associateBy { it.relationshipType }

    fun explainRelationship(
        target: IRI,
        relatedEntity: IRI,
        relationship: RelatedEntityRelationship,
        intermediateEntity: IRI?
    ): RelationshipExplanation {
        return explainers[relationship]!!.explain(target, relatedEntity, intermediateEntity)
    }
}
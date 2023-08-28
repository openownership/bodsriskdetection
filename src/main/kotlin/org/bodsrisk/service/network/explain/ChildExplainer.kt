package org.bodsrisk.service.network.explain

import jakarta.inject.Singleton
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.service.network.GraphType
import org.bodsrisk.service.network.RelationshipExplanation
import org.eclipse.rdf4j.model.IRI

@Singleton
class ChildExplainer : RelationshipExplainer(RelatedEntityRelationship.CHILD) {

    override fun explain(
        target: IRI,
        relatedEntity: IRI,
        intermediateEntity: IRI?
    ): RelationshipExplanation {
        return RelationshipExplanation(
            root = target,
            graph = networkService.relationshipChain(target, relatedEntity),
            graphType = GraphType.Tree,
            relevantNodes = setOf(target, relatedEntity)
        )
    }
}

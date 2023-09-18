package org.bodsrisk.service.network.explain

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.model.graph.GraphNodeType
import org.bodsrisk.service.network.GraphType
import org.bodsrisk.service.network.RelationshipExplanation
import org.eclipse.rdf4j.model.IRI
import org.rdf4k.toIri

@Singleton
class SameRegisteredAddressExplainer : RelationshipExplainer(RelatedEntityRelationship.SAME_REGISTERED_ADDRESS) {

    override fun explain(target: IRI, relatedEntity: IRI, intermediateEntity: IRI?): RelationshipExplanation {
        val graph = networkService.sameAddressGraph(target, relatedEntity)
        return RelationshipExplanation(
            root = graph.nodes.find { it.type == GraphNodeType.ADDRESS }!!.id.toIri(),
            graph = graph,
            graphType = GraphType.ReverseTree,
            relevantNodes = setOf(target, relatedEntity)
        )
    }
}
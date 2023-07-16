package org.bodsrisk.controller.relationships

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.controller.tree.ExtraNode
import org.bodsrisk.controller.tree.TreeResponse
import org.bodsrisk.service.network.GraphType
import org.bodsrisk.service.network.RelationshipExplanation
import org.bodsrisk.utils.toJsonString
import org.eclipse.rdf4j.model.IRI

@Introspected
data class ExplainRelationshipResponse(
    val target: IRI,
    val explanation: RelationshipExplanation
) {
    val tree: TreeResponse by lazy {
        val extraNode = explanation.extraNode?.let {
            ExtraNode(
                relatedTo = explanation.intermediateEntity!!,
                node = explanation.extraNode,
                relationshipDetails = explanation.extraNodeRelationshipDetails,

                // Using INCOMING in all cases for now, as we query the relationship from root to extraNode. From UBO to Relative, for example.
                relationshipDirection = ExtraNode.Direction.INCOMING
            )
        }
        TreeResponse.fromGraph(target.toString(), explanation.graph, explanation.relevantNodesStr, extraNode)
    }

    val reverse = explanation.graphType == GraphType.ReverseTree
    val treeJson = tree.toJsonString()
}

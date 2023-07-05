package org.bodsrisk.controller.tree

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.BodsGraph

@Introspected
data class TreeResponse(
    val rootId: String,
    val nodes: List<TreeNode>,
    val relationships: List<TreeRelationship>,
    val highlightedNodes: Set<String> = mutableSetOf(),
    val extraNode: ExtraNode?
) {

    companion object {
        fun fromGraph(
            rootId: String,
            graph: BodsGraph,
            highlightedNodes: Set<String>,
            extraNode: ExtraNode? = null
        ): TreeResponse {
            return TreeResponse(
                rootId = rootId,
                nodes = graph.nodes.map { node ->
                    TreeNode(
                        entity = node.entity,
                        risks = node.data.risks,
                        publicContractCount = node.data.publicContracts,
                        highlighted = highlightedNodes.contains(node.entity.iri.toString())
                    )
                },
                relationships = graph.relationships.map { TreeRelationship(it) },
                highlightedNodes = highlightedNodes.map { it }.toSet(),
                extraNode = extraNode
            )
        }
    }
}


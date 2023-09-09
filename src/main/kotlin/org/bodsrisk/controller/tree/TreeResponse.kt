package org.bodsrisk.controller.tree

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.RiskGraph

/**
 * A structure used for UI rendering of a (risk) Graph
 */
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
            graph: RiskGraph,
            highlightedNodes: Set<String>,
            extraNode: ExtraNode? = null
        ): TreeResponse {
            return TreeResponse(
                rootId = rootId,
                nodes = graph.nodes.map { node ->
                    TreeNode(node, highlightedNodes.contains(node.id))
                },
                relationships = graph.relationships.map { TreeRelationship(it) },
                highlightedNodes = highlightedNodes.map { it }.toSet(),
                extraNode = extraNode
            )
        }
    }
}


package org.bodsrisk.controller.tree

import org.bodsrisk.model.RiskNodeData
import org.bodsrisk.model.graph.GraphNode

data class ExtraNode(
    val node: TreeNode,
    val relationship: TreeRelationship,
    val relatedTo: String,
    val relationshipDirection: Direction
) {

    constructor(
        relatedTo: String,
        node: GraphNode<RiskNodeData>,
        relationshipDetails: List<String>,
        relationshipDirection: Direction
    ) : this(
        node = TreeNode(node, true),
        relatedTo = relatedTo,
        relationship = TreeRelationship(node.id, relatedTo, relationshipDetails),
        relationshipDirection = relationshipDirection
    )

    enum class Direction {
        INCOMING,
        OUTGOING
    }
}
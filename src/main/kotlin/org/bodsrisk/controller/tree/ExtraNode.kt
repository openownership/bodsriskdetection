package org.bodsrisk.controller.tree

import org.bodsrisk.model.BodsNodeData
import org.bodsrisk.model.graph.GraphNode

data class ExtraNode(
    val node: TreeNode,
    val relationship: TreeRelationship,
    val relatedTo: String,
    val relationshipDirection: Direction
) {

    constructor(
        relatedTo: String,
        node: GraphNode<BodsNodeData>,
        relationshipDetails: List<String>,
        relationshipDirection: Direction
    ) : this(
        node = TreeNode(node.entity, node.data.risks, 0, true),
        relatedTo = relatedTo,
        relationship = TreeRelationship(node.entity.iri.toString(), relatedTo, relationshipDetails),
        relationshipDirection = relationshipDirection
    )

    enum class Direction {
        INCOMING,
        OUTGOING
    }
}
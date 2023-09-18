package org.bodsrisk.controller.tree

import org.bodsrisk.model.RiskGraphNode
import org.bodsrisk.model.graph.GraphNodeType
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.utils.plural

data class TreeNode(
    val id: String,
    val name: String,
    val type: String,
    val tags: List<NodeTag>,
    var highlighted: Boolean = false,
    val showLink: Boolean = true,
) {

    constructor(
        node: RiskGraphNode,
        highlighted: Boolean = false
    ) : this(
        id = node.id,
        name = node.name,
        type = node.type.name,
        tags = (node.data?.risks ?: emptyList()).toTags()
            .plus(contractTags(node.data?.publicContracts ?: 0)),
        highlighted = highlighted,
        showLink = node.type != GraphNodeType.ADDRESS
    )

}

private fun contractTags(publicContractCount: Int): List<NodeTag> {
    return if (publicContractCount > 0) {
        listOf(NodeTag(publicContractCount.plural("Contract", "Contracts"), TagType.CONTRACTS, TagColor.AMBER))
    } else {
        emptyList()
    }
}

private fun List<Risk>.toTags(): List<NodeTag> {
    return if (isNotEmpty()) {
        listOf(NodeTag(this.size.plural("Risk", "Risks"), TagType.RISKS, TagColor.RED))
    } else {
        emptyList()
    }
}
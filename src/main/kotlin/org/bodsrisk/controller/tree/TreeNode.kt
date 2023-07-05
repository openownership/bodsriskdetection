package org.bodsrisk.controller.tree

import org.bodsrisk.model.Entity
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.utils.plural

data class TreeNode(
    val id: String,
    val name: String,
    val type: String,
    val tags: List<NodeTag>,
    var highlighted: Boolean = false
) {

    constructor(
        entity: Entity,
        risks: List<Risk>,
        publicContractCount: Int,
        highlighted: Boolean = false
    ) : this(
        id = entity.iri.toString(),
        name = entity.name,
        type = entity.type.name,
        tags = risks.toTags()
            .plus(contractTags(publicContractCount)),
        highlighted = highlighted
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
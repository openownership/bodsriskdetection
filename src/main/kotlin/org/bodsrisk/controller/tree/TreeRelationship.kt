package org.bodsrisk.controller.tree

import org.bodsrisk.model.graph.Relationship

data class TreeRelationship(
    val parentId: String,
    val childId: String,
    val details: List<String>
) {
    constructor(relationship: Relationship) : this(
        parentId = relationship.parentId,
        childId = relationship.childId,
        details = relationship.details
    )
}
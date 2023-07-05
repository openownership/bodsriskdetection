package org.bodsrisk.model.graph

data class Relationship(
    val parentId: String,
    val childId: String,
    val details: List<String>
)

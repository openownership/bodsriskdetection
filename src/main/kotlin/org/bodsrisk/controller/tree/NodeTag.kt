package org.bodsrisk.controller.tree

data class NodeTag(
    val label: String,
    val type: TagType,
    val color: TagColor,
)

enum class TagColor {
    RED,
    AMBER
}

enum class TagType {
    RISKS,
    CONTRACTS
}
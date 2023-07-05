package org.bodsrisk.model

data class CorporateGroup(
    val ultimateParentId: String,
    val graph: BodsGraph
)

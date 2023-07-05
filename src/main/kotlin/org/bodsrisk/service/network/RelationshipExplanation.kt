package org.bodsrisk.service.network

import org.bodsrisk.model.BodsGraph
import org.bodsrisk.model.BodsNodeData
import org.bodsrisk.model.graph.GraphNode
import org.bodsrisk.rdf.asStrings
import org.eclipse.rdf4j.model.IRI

data class RelationshipExplanation(
    val graph: BodsGraph,
    val graphType: GraphType,
    val relevantNodes: Set<IRI>,
    val extraNode: GraphNode<BodsNodeData>? = null,
    val extraNodeRelationshipDetails: List<String> = emptyList(),
    val intermediateEntity: String? = null
) {

    val relevantNodesStr: Set<String> = relevantNodes.asStrings().toSet()
}

enum class GraphType {
    Tree,
    ReverseTree,
}
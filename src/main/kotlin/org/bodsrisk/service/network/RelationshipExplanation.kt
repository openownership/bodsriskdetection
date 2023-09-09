package org.bodsrisk.service.network

import org.bodsrisk.model.RiskGraph
import org.bodsrisk.model.RiskNodeData
import org.bodsrisk.model.graph.GraphNode
import org.bodsrisk.rdf.asStrings
import org.eclipse.rdf4j.model.IRI

data class RelationshipExplanation(
    val root: IRI,
    val graph: RiskGraph,
    val graphType: GraphType,
    val relevantNodes: Set<IRI> = emptySet(),
    val extraNode: GraphNode<RiskNodeData>? = null,
    val extraNodeRelationshipDetails: List<String> = emptyList(),
    val intermediateEntity: String? = null
) {

    val relevantNodesStr: Set<String> = relevantNodes.asStrings().toSet()
}

enum class GraphType {
    Tree,
    ReverseTree,
}
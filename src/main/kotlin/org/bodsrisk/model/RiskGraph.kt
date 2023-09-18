package org.bodsrisk.model

import org.bodsrisk.model.graph.Graph
import org.bodsrisk.model.graph.GraphNode
import org.bodsrisk.model.graph.GraphNodeType
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.rdf.templateEntitiesQuery
import org.bodsrisk.service.risk.RiskService
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.str
import org.rdf4k.toIri

data class RiskNodeData(
    val risks: List<Risk> = emptyList(),
    val publicContracts: Int = 0
)

typealias RiskGraph = Graph<RiskNodeData>
typealias RiskGraphNode = GraphNode<RiskNodeData>

fun EntityType.toNodeType(): GraphNodeType {
    return when (this) {
        EntityType.LEGAL_ENTITY -> GraphNodeType.LEGAL_ENTITY
        EntityType.PERSON -> GraphNodeType.PERSON
        EntityType.UNKNOWN -> GraphNodeType.UNKNOWN
    }
}

fun Entity.toRiskGraphNode(data: RiskNodeData? = null): RiskGraphNode {
    return RiskGraphNode(
        id = iri.toString(),
        name = name,
        type = type.toNodeType(),
        data = data
    )
}

fun emptyRiskGraph() = Graph.empty<RiskNodeData>()

/**
 * This is rather dirty. The reason is that we want to keep the core queries that produce the graph
 * simple (subsidiaries, UBO relatives etc.) and add this extra info as another layer on top of graph.
 * In a real world application this needs a rethink.
 */
fun RiskGraph.addNodeData(
    riskService: RiskService,
    rdfRepository: Repository
): RiskGraph {
    val entityIris = nodes.map { it.id.toIri() }
    val risks = riskService.risksForEntities(entityIris)

    val contractCounts =
        rdfRepository.templateEntitiesQuery(
            "sparql/public-contracts/counts-for-entities.template.sparql",
            entityIris
        ).contractCounts()

    nodes.forEach { node ->
        node.data = RiskNodeData(
            risks = risks[node.id.toIri()] ?: emptyList(),
            publicContracts = contractCounts[node.id.toIri()] ?: 0
        )
    }
    return this
}

private fun List<BindingSet>.contractCounts(): Map<IRI, Int> {
    return associate {
        val contractCount = it.str("publicContracts").toInt()
        it.iri("entity") to contractCount
    }
}

package org.bodsrisk.model

import org.bodsrisk.model.graph.Graph
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.rdf.templateEntitiesQuery
import org.bodsrisk.service.risk.RiskService
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.str

data class BodsNodeData(
    val risks: List<Risk> = emptyList(),
    val publicContracts: Int = 0
)

typealias BodsGraph = Graph<BodsNodeData>

/**
 * This is rather dirty. The reason is that we want to keep the core queries that produce the graph
 * simple (subsidiaries, UBO relatives etc.) and add this extra info as another layer on top of graph.
 * In a real world application this needs a rethink.
 */
fun BodsGraph.addNodeData(
    riskService: RiskService,
    rdfRepository: Repository
): BodsGraph {
    val entityIds = nodes.map { it.entity.iri }
    val risks = riskService.risksForEntities(entityIds)

    val contractCounts =
        rdfRepository.templateEntitiesQuery(
            "sparql/public-contracts/counts-for-entities.template.sparql",
            entityIds
        ).contractCounts()

    nodes.forEach { node ->
        node.data = BodsNodeData(
            risks = risks[node.entity.iri] ?: emptyList(),
            publicContracts = contractCounts[node.entity.iri] ?: 0
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

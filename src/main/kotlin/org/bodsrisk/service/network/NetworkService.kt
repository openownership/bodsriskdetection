package org.bodsrisk.service.network

import jakarta.inject.Singleton
import org.bodsrisk.model.*
import org.bodsrisk.model.graph.Graph
import org.bodsrisk.model.graph.GraphNode
import org.bodsrisk.model.graph.Relationship
import org.bodsrisk.rdf.allValues
import org.bodsrisk.rdf.asStrings
import org.bodsrisk.service.risk.RiskService
import org.bodsrisk.service.entityresolver.EntityResolver
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.str

@Singleton
class NetworkService(
    private val rdfRepository: Repository,
    private val entityResolver: EntityResolver,
    private val riskService: RiskService
) {

    fun uboGraph(target: IRI): BodsGraph {
        val rows = rdfRepository.sparqlSelectClasspath("sparql/network/ubo-chains.sparql", "target" to target)
        val parentIds = rows.map { it.iri("ubo") }.toSet()
        return graph(rows)
            .shortestPaths(parentIds.asStrings(), target.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun relationshipChain(source: IRI, destination: IRI): BodsGraph {
        val rows = rdfRepository.sparqlSelectClasspath(
            "sparql/network/relationship-chain.sparql",
            "source" to source,
            "destination" to destination
        )
        return graph(rows)
            .shortestPath(source.toString(), destination.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun parentCompanies(target: IRI): BodsGraph {
        var graph = Graph.empty<BodsNodeData>()
        val ultimateParents = ultimateParents(target)
        ultimateParents.forEach { ultimateParentId ->
            graph += childCompanies(ultimateParentId)
        }
        return graph
            .shortestPaths(ultimateParents.asStrings(), target.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun corporateGroup(target: IRI): CorporateGroup {
        var graph = Graph.empty<BodsNodeData>()
        val ultimateParents = ultimateParents(target)
        ultimateParents.forEach { ultimateParentId ->
            graph += childCompanies(ultimateParentId)
        }
        //TODO: Add synthetic node when there are more ultimate parents
        return CorporateGroup(
            ultimateParentId = ultimateParents.first().toString(),
            graph = graph.addNodeData(riskService, rdfRepository)
        )
    }

    fun getFamilyRelationship(from: IRI, to: IRI): List<String> {
        return rdfRepository
            .sparqlSelectClasspath("sparql/network/get-family-relationship.sparql", "from" to from, "to" to to)
            .map { it.str("relationship") }
    }

    fun getAssociateRelationship(from: IRI, to: IRI): List<String> {
        return rdfRepository
            .sparqlSelectClasspath("sparql/network/get-associate-relationship.sparql", "from" to from, "to" to to)
            .map { it.str("relationship") }
    }

    private fun graph(bindings: Collection<BindingSet>): BodsGraph {
        val entities = entityResolver.resolveEntity(bindings.allValues(RdfConst.FIELD_PARENT, RdfConst.FIELD_CHILD))
        val relationships = entityResolver.resolveRelationships(bindings.allValues(RdfConst.FIELD_CTRL_STATEMENT))
        return Graph(
            nodes = entities.values.map { GraphNode(it, BodsNodeData()) },
            relationships = bindings.toRelationships { relationships[it]!! }.toSet()
        )
    }

    fun childCompanies(parent: IRI): BodsGraph {
        val rows = rdfRepository.sparqlSelectClasspath(
            "sparql/network/child-companies.sparql",
            "ultimateParent" to parent
        )
        return graph(rows).addNodeData(riskService, rdfRepository)
    }

    private fun ultimateParents(target: IRI): Collection<IRI> {
        return rdfRepository
            .sparqlSelectClasspath("sparql/network/ultimate-parents.sparql", "target" to target)
            .map { it.iri("ultimateParent") }
            .toSet()
    }

}

private fun bodsRelationship(row: BindingSet, getRelationship: (IRI) -> Relationship): Relationship {
    return Relationship(
        parentId = row.iri("parent").toString(),
        childId = row.iri("child").toString(),
        details = getRelationship(row.iri("controlStatement")).details
    )
}

private fun Collection<BindingSet>.toRelationships(getRelationship: (IRI) -> Relationship): List<Relationship> {
    return this.map { bodsRelationship(it, getRelationship) }
}

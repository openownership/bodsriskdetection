package org.bodsrisk.service.network

import io.slink.id.uuid
import jakarta.inject.Singleton
import org.bodsrisk.model.*
import org.bodsrisk.model.graph.GraphNodeType
import org.bodsrisk.model.graph.Relationship
import org.bodsrisk.rdf.allValues
import org.bodsrisk.rdf.asStrings
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.service.entityresolver.EntityResolver
import org.bodsrisk.service.risk.RiskService
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.str
import org.rdf4k.toIri
import org.slf4j.LoggerFactory

@Singleton
class NetworkService(
    private val rdfRepository: Repository,
    private val entityResolver: EntityResolver,
    private val riskService: RiskService
) {

    fun uboGraph(target: IRI): RiskGraph {
        val rows = rdfRepository.sparqlSelectClasspath("sparql/network/ubo-chains.sparql", "target" to target)
        val parentIds = rows.map { it.iri("ubo") }.toSet()
        return graph(rows)
            .shortestPaths(parentIds.asStrings(), target.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun relationshipChain(source: IRI, destination: IRI): RiskGraph {
        val rows = rdfRepository.sparqlSelectClasspath(
            "sparql/network/relationship-chain.sparql",
            "source" to source,
            "destination" to destination
        )
        return graph(rows)
            .shortestPath(source.toString(), destination.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun sameAddressGraph(entity1: IRI, entity2: IRI): RiskGraph {
        val node1 = entityResolver.resolveEntities(entity1)!!.toRiskGraphNode()
        val node2 = entityResolver.resolveEntities(entity2)!!.toRiskGraphNode()

        val fullAddress = rdfRepository.sparqlSelectClasspath(
            "sparql/network/get-common-registered-address.sparql",
            "entity1" to entity1,
            "entity2" to entity2,
        ).first()
            .str("fullAddress")

        val addressIri = BodsRisk.entity(uuid())
        val address = RiskGraphNode(
            id = addressIri.toString(),
            name = fullAddress,
            type = GraphNodeType.ADDRESS,
        )

        val sameAddressGraph = RiskGraph(
            nodes = listOf(node1, node2, address),
            relationships = setOf(
                Relationship(
                    parentId = entity1.toString(),
                    childId = addressIri.toString(),
                    listOf("Registered address")
                ),
                Relationship(
                    parentId = entity2.toString(),
                    childId = addressIri.toString(),
                    listOf("Registered address")
                )
            )
        )
        return sameAddressGraph.addNodeData(riskService, rdfRepository)
    }

    fun parentCompanies(target: IRI): RiskGraph {
        var graph = emptyRiskGraph()
        val ultimateParents = ultimateParents(target)
        ultimateParents.forEach { ultimateParentId ->
            graph += childCompanies(ultimateParentId)
        }
        return graph
            .shortestPaths(ultimateParents.asStrings(), target.toString())
            .addNodeData(riskService, rdfRepository)
    }

    fun corporateGroup(target: IRI): CorporateGroup {
        var graph = emptyRiskGraph()
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

    private fun graph(bindings: Collection<BindingSet>): RiskGraph {
        val entities = entityResolver.resolveEntities(bindings.allValues(RdfConst.FIELD_PARENT, RdfConst.FIELD_CHILD))
        val relationships = entityResolver.resolveRelationships(bindings.allValues(RdfConst.FIELD_CTRL_STATEMENT))
        return RiskGraph(
            nodes = entities.values.map { it.toRiskGraphNode() },
            relationships = bindings
                .toRelationships { relationships[it]!! }
                .filter {
                    it.parentId.toIri() in entities.keys && it.childId.toIri() in entities.keys
                }
                .toSet()
        )
    }

    fun childCompanies(parent: IRI): RiskGraph {
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

    companion object {
        private val log = LoggerFactory.getLogger(NetworkService::class.java)
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

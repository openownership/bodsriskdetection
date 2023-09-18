package org.bodsrisk.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import jakarta.inject.Singleton
import org.bodsrisk.data.publiccontracts.ContractsFinderDataset
import org.bodsrisk.elasticsearch.findByIds
import org.bodsrisk.model.*
import org.bodsrisk.model.ocds.PublicContract
import org.bodsrisk.rdf.allValues
import org.bodsrisk.rdf.localNames
import org.bodsrisk.service.network.NetworkService
import org.bodsrisk.service.risk.RiskService
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.str

@Singleton
class PublicContractsService(
    private val esClient: ElasticsearchClient,
    private val rdfRepository: Repository,
    private val openOwnershipService: BodsService,
    private val networkService: NetworkService,
    private val riskService: RiskService
) {

    fun getPublicContracts(target: IRI): PublicContracts {
        val results = rdfRepository.sparqlSelectClasspath(
            "sparql/public-contracts/contracts-for-entity.sparql",
            "target" to target
        )
        val contractIds = results.localNames("contract")
        return PublicContracts(
            contracts = getContracts(contractIds).values.toList()
        )
    }

    fun childCompaniesContracts(target: Entity): RiskGraph {
        var graph = emptyRiskGraph()

        rdfRepository
            .sparqlSelectClasspath(
                "sparql/public-contracts/related-entities-with-contracts.sparql",
                "target" to target.iri
            )
            .allValues<IRI>("entity")
            .forEach { relatedEntityId ->
                graph += networkService.relationshipChain(target.iri, relatedEntityId)
            }
        return graph.addNodeData(riskService, rdfRepository)
    }

    fun getRelatedEntitiesContracts(target: IRI): List<RelatedEntity<PublicContracts>> {
        val results = rdfRepository.sparqlSelectClasspath(
            "sparql/public-contracts/contracts-for-related-entities.sparql",
            "target" to target
        )

        val contracts = getContracts(results)
        val statements = openOwnershipService.getStatements(results, "relatedEntity")

        return contracts.map {
            val entity = statements[it.key]!!
            RelatedEntity(
                target = target,
                statement = entity,
                relationship = RelatedEntityRelationship.CHILD,
                data = PublicContracts(it.value)
            )
        }
    }

    private fun getContracts(results: List<BindingSet>): Map<String, List<PublicContract>> {
        val entityContracts = mutableMapOf<String, MutableList<PublicContract>>()
        val contractIds = results.localNames("contract")
        val contracts = getContracts(contractIds)
        results.forEach {
            val entityId = it.iri("relatedEntity").localName
            val contractId = it.iri("contract").localName
            entityContracts.putIfAbsent(entityId, mutableListOf())
            entityContracts[entityId]!!.add(contracts[contractId]!!)
        }
        return entityContracts
    }

    private fun List<BindingSet>.contractCounts(): Map<IRI, Int> {
        return associate {
            val contractCount = it.str("publicContracts").toInt()
            it.iri("entity") to contractCount
        }
    }

    private fun getContracts(contractIds: Collection<String>): Map<String, PublicContract> {
        return esClient.findByIds<PublicContract>(ContractsFinderDataset.INDEX, contractIds)
    }
}

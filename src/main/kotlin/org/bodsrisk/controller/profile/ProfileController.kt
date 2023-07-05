package org.bodsrisk.controller.profile

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.views.View
import org.bodsrisk.controller.tree.TreeResponse
import org.bodsrisk.service.BodsService
import org.bodsrisk.service.PublicContractsService
import org.bodsrisk.service.risk.RiskService
import org.bodsrisk.service.entityresolver.EntityResolver
import org.bodsrisk.service.network.NetworkService
import org.bodsrisk.utils.toJsonString
import org.eclipse.rdf4j.model.IRI

@Controller
class ProfileController(
    private val bodsService: BodsService,
    private val publicContractsService: PublicContractsService,
    private val riskService: RiskService,
    private val networkService: NetworkService,
    private val entityResolver: EntityResolver,
) {

    @Get("/profile/{targetId}/risks")
    @View("profile/risks")
    fun risks(@PathVariable("targetId") targetId: IRI): TargetRisksResponse {
        val target = entityResolver.resolveEntity(targetId)!!
        return TargetRisksResponse(
            target = target,
            riskProfile = riskService.getRiskProfile(targetId),
            relatedEntities = riskService.relatedEntitiesRisks(target)
        )
    }

    @Get("/profile/{targetId}/public-contracts")
    @View("profile/public-contracts/awarded")
    fun publicContracts(@PathVariable("targetId") targetId: IRI): PublicContractsResponse {
        val target = entityResolver.resolveEntity(targetId)!!
        return PublicContractsResponse(
            target = target,
            publicContracts = publicContractsService.getPublicContracts(targetId)
        )
    }

    @Get("/profile/{targetId}/public-contracts/related-entities")
    @View("profile/public-contracts/related-entities")
    fun publicContractsSubsidiaries(@PathVariable("targetId") targetId: IRI): RelatedPublicContractsResponse {
        val target = entityResolver.resolveEntity(targetId)!!
        return RelatedPublicContractsResponse(
            target = target,
            relatedEntities = publicContractsService.getRelatedEntitiesContracts(targetId)
        )
    }

    @Get("/profile/{targetId}/public-contracts/network")
    @View("profile/public-contracts/subsidiaries-contracts")
    fun publicContractsNetwork(@PathVariable("targetId") targetId: IRI): ProfileResponse<String> {
        val target = entityResolver.resolveEntity(targetId)!!
        val graph = publicContractsService.childCompaniesContracts(target)
        val highlightedNodes = setOf(targetId.toString())
            .plus(graph.nodes
                .filter { it.data.publicContracts > 0 }
                .map { it.entity.iri.toString() }
            )
        return TreeResponse.fromGraph(targetId.toString(), graph, highlightedNodes)
            .profileTreeResponse(targetId)
    }

    @Get("/profile/{target}/ubos")
    @View("profile/ubos")
    fun ubos(@PathVariable("target") targetId: IRI): ProfileResponse<String> {
        val graph = networkService.uboGraph(targetId)
        val highlightedNodes = setOf(targetId.toString())
            .plus(graph.nodes.filter { it.entity.isPerson }.map { it.entity.iri.toString() })
        return TreeResponse.fromGraph(targetId.toString(), graph, highlightedNodes)
            .profileTreeResponse(targetId)
    }

    @Get("/profile/{targetId}/corporate-group")
    @View("profile/corporate-group")
    fun corporateGroup(@PathVariable("targetId") targetId: IRI): ProfileResponse<String> {
        val corporateGroup = networkService.corporateGroup(targetId)
        return TreeResponse.fromGraph(corporateGroup.ultimateParentId, corporateGroup.graph, setOf(targetId.toString()))
            .profileTreeResponse(targetId)
    }

    @Get("/profile/{targetId}/child-companies")
    @View("profile/child-companies")
    fun subsidiaries(@PathVariable("targetId") targetId: IRI): ProfileResponse<String> {
        val childCompanies = networkService.childCompanies(targetId)
        return TreeResponse.fromGraph(targetId.toString(), childCompanies, setOf(targetId.toString()))
            .profileTreeResponse(targetId)
    }

    @Get("/profile/{targetId}/parent-companies")
    @View("profile/parent-companies")
    fun parentCompanies(@PathVariable("targetId") targetId: IRI): ProfileResponse<String> {
        val childCompanies = networkService.parentCompanies(targetId)
        return TreeResponse.fromGraph(targetId.toString(), childCompanies, setOf(targetId.toString()))
            .profileTreeResponse(targetId)
    }

    private fun TreeResponse.profileTreeResponse(target: IRI): ProfileResponse<String> {
        return ProfileResponse(
            target = entityResolver.resolveEntity(target)!!,
            content = this.toJsonString()
        )
    }
}


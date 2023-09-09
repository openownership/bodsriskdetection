package org.bodsrisk.service.network.explain

import io.slink.string.titleCaseFirstChar
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.bodsrisk.model.RiskNodeData
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.model.toRiskGraphNode
import org.bodsrisk.service.network.GraphType
import org.bodsrisk.service.network.RelationshipExplanation
import org.bodsrisk.service.risk.RiskService
import org.eclipse.rdf4j.model.IRI

@Singleton
class UboRelativeExplainer : RelationshipExplainer(RelatedEntityRelationship.UBO_RELATIVE) {

    @Inject
    lateinit var riskService: RiskService

    override fun explain(
        target: IRI,
        relatedEntity: IRI,
        intermediateEntity: IRI?
    ): RelationshipExplanation {
        val familyRelationship = networkService.getFamilyRelationship(relatedEntity, intermediateEntity!!)
        val extraEntity = entityResolver.resolveEntities(relatedEntity)!!
        val risks = riskService.risksForEntity(relatedEntity)

        return RelationshipExplanation(
            root = target,
            graph = networkService.relationshipChain(intermediateEntity, target),
            graphType = GraphType.ReverseTree,
            relevantNodes = setOf(target, relatedEntity),
            extraNode = extraEntity.toRiskGraphNode(RiskNodeData(risks = risks)), // TODO: This needs to move in a service
            extraNodeRelationshipDetails = familyRelationship.map { it.titleCaseFirstChar() },
            intermediateEntity = intermediateEntity.toString()
        )
    }
}

package org.bodsrisk.service.network.explain

import io.slink.string.titleCaseFirstChar
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.bodsrisk.model.BodsNodeData
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.model.graph.GraphNode
import org.bodsrisk.service.risk.RiskService
import org.bodsrisk.service.network.GraphType
import org.bodsrisk.service.network.RelationshipExplanation
import org.eclipse.rdf4j.model.IRI

//TODO: This is a lot of duplicate code with UboRelativeExplainer, maybe worth extracting an abstraction
@Singleton
class UboAssociateExplainer : RelationshipExplainer(RelatedEntityRelationship.UBO_ASSOCIATE) {

    @Inject
    lateinit var riskService: RiskService

    override fun explain(
        target: IRI,
        relatedEntity: IRI,
        intermediateEntity: IRI?
    ): RelationshipExplanation {
        val familyRelationship = networkService.getAssociateRelationship(relatedEntity, intermediateEntity!!)
        val extraEntity = entityResolver.resolveEntity(relatedEntity)!!
        val risks = riskService.risksForEntity(relatedEntity)

        return RelationshipExplanation(
            graph = networkService.relationshipChain(intermediateEntity, target),
            graphType = GraphType.ReverseTree,
            relevantNodes = setOf(target, relatedEntity),
            extraNode = GraphNode(extraEntity, BodsNodeData(risks = risks)), // TODO: This needs to move in a service
            extraNodeRelationshipDetails = familyRelationship.map { it.titleCaseFirstChar() },
            intermediateEntity = intermediateEntity.toString()
        )
    }
}

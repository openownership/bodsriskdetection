package org.bodsrisk.service.risk.indirect

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship

@Singleton
class UboAssociateRisksResolver : IndirectRiskResolver() {

    override val entityTypes = setOf(EntityType.LEGAL_ENTITY)
    override val relationshipType = RelatedEntityRelationship.UBO_ASSOCIATE
    override val order = 4
    override val findQueryPath = "sparql/risk/find-risks-for-ubo-associates.sparql"
}

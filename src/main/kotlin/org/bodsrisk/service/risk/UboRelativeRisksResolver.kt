package org.bodsrisk.service.risk

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship

@Singleton
class UboRelativeRisksResolver : RelatedRiskResolver() {

    override val entityTypes = setOf(EntityType.LEGAL_ENTITY)
    override val relationshipType = RelatedEntityRelationship.UBO_RELATIVE
    override val order = 3
    override val findQueryPath = "sparql/risk/find-risks-for-ubo-relatives.sparql"
}

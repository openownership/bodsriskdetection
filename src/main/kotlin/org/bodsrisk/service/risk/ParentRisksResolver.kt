package org.bodsrisk.service.risk

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship

@Singleton
class ParentRisksResolver : RelatedRiskResolver() {

    override val entityTypes = setOf(EntityType.LEGAL_ENTITY)
    override val relationshipType = RelatedEntityRelationship.PARENT
    override val order = 1
    override val findQueryPath = "sparql/risk/find-risks-for-parents.sparql"
}
package org.bodsrisk.service.risk.indirect

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.service.risk.indirect.IndirectRiskResolver

@Singleton
class ChildrenRisksResolver : IndirectRiskResolver() {

    override val entityTypes = setOf(EntityType.LEGAL_ENTITY, EntityType.PERSON)
    override val relationshipType = RelatedEntityRelationship.CHILD
    override val order = 2
    override val findQueryPath = "sparql/risk/find-risks-for-children.sparql"
}
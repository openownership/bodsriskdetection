package org.bodsrisk.service.risk.indirect

import jakarta.inject.Singleton
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntityRelationship

@Singleton
class RegisteredAddressRisksResolver : IndirectRiskResolver() {

    override val entityTypes = setOf(EntityType.LEGAL_ENTITY)
    override val relationshipType = RelatedEntityRelationship.SAME_REGISTERED_ADDRESS
    override val order = 5
    override val findQueryPath = "sparql/risk/find-risks-for-registered-address.sparql"
}
package org.bodsrisk.controller.profile

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.Entity
import org.bodsrisk.model.RelatedEntity
import org.bodsrisk.model.risk.RiskProfile
import org.bodsrisk.model.risk.Risks

@Introspected
data class TargetRisksResponse(
    val target: Entity,
    val riskProfile: RiskProfile?,
    val relatedEntities: List<RelatedEntity<Risks>>
)

package org.bodsrisk.controller.profile

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.Entity
import org.bodsrisk.model.PublicContracts
import org.bodsrisk.model.RelatedEntity
import org.kbods.read.BodsStatement

@Introspected
data class RelatedPublicContractsResponse(
    val target: Entity,
    val relatedEntities: List<RelatedEntity<PublicContracts>>
) {
    val totalContracts: Int = relatedEntities.sumOf { it.data.contracts.size }
    val totalValue: Double = relatedEntities.sumOf { it.data.totalValue }
}
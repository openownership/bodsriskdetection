package org.bodsrisk.controller.profile

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.Entity
import org.bodsrisk.model.PublicContracts

@Introspected
data class PublicContractsResponse(
    val target: Entity,
    val publicContracts: PublicContracts
)
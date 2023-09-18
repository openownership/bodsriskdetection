package org.bodsrisk.controller.profile

import io.micronaut.core.annotation.Introspected
import org.bodsrisk.model.Entity

@Introspected
data class ProfileResponse<T>(
    val target: Entity,
    val content: T
)
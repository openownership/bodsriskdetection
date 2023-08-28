package org.bodsrisk.controller.search

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.model.Page
import org.bodsrisk.model.Entity
import org.bodsrisk.model.EntityType

@Introspected
data class SearchResponse(
    val q: String?,
    val entityType: EntityType?,
    val page: Page<Entity>
) {

    val emptyState: Boolean = q == null
}
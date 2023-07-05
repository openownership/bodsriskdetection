package org.bodsrisk.controller.search

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.views.View
import io.slink.string.nullIfBlank
import org.bodsrisk.model.EntityType
import org.bodsrisk.service.BodsService
import org.kbods.read.BodsStatementType

@Controller
class SearchController(
    private val openOwnershipService: BodsService
) {

    @Get("/search")
    @View("search/search")
    fun search(
        @QueryValue("entityType") entityType: EntityType?,
        @QueryValue("q") q: String?,
        pageable: Pageable
    ): SearchResponse {
        val searchTerm = q.nullIfBlank()
        val page = if (searchTerm != null) {
            openOwnershipService.search(searchTerm, entityType, pageable)
        } else {
            Page.empty()
        }
        return SearchResponse(
            q = searchTerm,
            entityType = entityType,
            page = page
        )
    }
}

package org.bodsrisk.controller.relationships

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.views.View
import org.bodsrisk.service.network.RelationshipService

@Controller
class RelationshipsController(
    private val relationshipService: RelationshipService
) {

    @Post("/relationships/explain")
    @Consumes(MediaType.APPLICATION_JSON)
    @View("profile/explain-relationship")
    fun explainRelationship(@Body request: ExplainRelationshipRequest): ExplainRelationshipResponse {
        val explanation = relationshipService.explainRelationship(
            target = request.target,
            relatedEntity = request.relatedEntity,
            relationship = request.relationship,
            intermediateEntity = request.intermediateEntity
        )
        return ExplainRelationshipResponse(request.target, explanation)
    }
}


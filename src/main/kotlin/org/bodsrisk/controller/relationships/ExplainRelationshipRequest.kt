package org.bodsrisk.controller.relationships

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import org.bodsrisk.model.RelatedEntityRelationship
import org.eclipse.rdf4j.model.IRI

@Introspected
@Serdeable
data class ExplainRelationshipRequest(
    val target: IRI,
    val relationship: RelatedEntityRelationship,
    val relatedEntity: IRI,
    val intermediateEntity: IRI?
)

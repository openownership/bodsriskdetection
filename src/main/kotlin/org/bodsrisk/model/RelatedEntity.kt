package org.bodsrisk.model

import org.eclipse.rdf4j.model.IRI
import org.kbods.read.BodsStatement

data class RelatedEntity<T>(
    val target: IRI,
    val entity: Entity,
    val relationship: RelatedEntityRelationship,
    val data: T,
    val intermediateEntity: IRI? = null
) {

    fun relationshipLabel(targetType: EntityType) = relationship.label(targetType, entity.type)

    constructor(
        target: IRI,
        statement: BodsStatement,
        relationship: RelatedEntityRelationship,
        data: T
    ) : this(
        target = target,
        entity = statement.toEntity(),
        relationship = relationship,
        data = data
    )
}

package org.bodsrisk.model

import org.bodsrisk.utils.i18n.I18n

enum class RelatedEntityRelationship {
    PARENT,
    CHILD,
    UBO_RELATIVE,
    UBO_ASSOCIATE,
    SAME_REGISTERED_ADDRESS;

    fun label(targetType: EntityType, relatedEntityType: EntityType): String {
        return I18n.get("relatedEntities.${targetType}.${this.name}.${relatedEntityType}")
    }
}

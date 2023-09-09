package org.bodsrisk.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.slink.iso3166.Country
import org.bodsrisk.utils.json.IriDeserializer
import org.bodsrisk.utils.json.IriSerializer
import org.eclipse.rdf4j.model.IRI

/**
 * This is a deliberately "dumbed down" representation of an entity used to transfer basic information
 * about a person or company from the underlying record. Fields like dateOfBirth would require specific typing (LocalDate)
 * which we didn't bother with, due to the large number of fields in certain sources where a complete date isn't available.
 * We chose to simply transfer the underlying String value to avoid this.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "entity_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = LegalEntity::class, name = "LEGAL_ENTITY"),
    JsonSubTypes.Type(value = Person::class, name = "PERSON"),
    JsonSubTypes.Type(value = UnknownEntity::class, name = "UNKNOWN"),
)
abstract class Entity(
    val iri: IRI,
    val name: String,
    val type: EntityType,
    val source: DataSource
) {
    val id: String = iri.toString()
    val isPerson: Boolean = type == EntityType.PERSON
    val isLegalEntity: Boolean = type == EntityType.LEGAL_ENTITY
}

class LegalEntity(
    @JsonSerialize(using = IriSerializer::class)
    @JsonDeserialize(using = IriDeserializer::class)
    iri: IRI,
    name: String,
    source: DataSource,

    val registrationNumber: String?,
    val jurisdiction: Country?
) : Entity(
    iri = iri,
    name = name,
    type = EntityType.LEGAL_ENTITY,
    source = source
)

class Person(
    iri: IRI,
    name: String,
    source: DataSource,

    val dateOfBirth: String?,
    val nationalities: List<Country>
) : Entity(
    iri = iri,
    name = name,
    type = EntityType.PERSON,
    source = source
)

class UnknownEntity(
    @JsonSerialize(using = IriSerializer::class)
    @JsonDeserialize(using = IriDeserializer::class)
    iri: IRI,
    name: String,
    source: DataSource
) : Entity(
    iri = iri,
    name = name,
    type = EntityType.UNKNOWN,
    source = source
)

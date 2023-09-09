package org.bodsrisk.model

import co.elastic.clients.json.JsonData
import io.slink.iso3166.Country
import org.bodsrisk.data.openownership.interestDetails
import org.bodsrisk.data.opensanctions.entityType
import org.bodsrisk.model.graph.Relationship
import org.bodsrisk.rdf.vocabulary.FTM
import org.bodsrisk.utils.toKlaxonJson
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.iri
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.rdf4k.iri

internal val BodsStatementType.entityType: EntityType
    get() {
        return when (this) {
            BodsStatementType.ENTITY -> EntityType.LEGAL_ENTITY
            BodsStatementType.PERSON -> EntityType.PERSON
            else -> throw IllegalStateException("Statement is not an person or entity")
        }
    }

internal fun BodsStatement.toEntity(): Entity {
    return when (this.statementType.entityType) {
        EntityType.LEGAL_ENTITY -> LegalEntity(
            iri = this.iri(),
            name = this.name,
            source = DataSource.OpenOwnership,
            registrationNumber = this.identifier("GB-COH"),
            jurisdiction = this.jurisdictionCode?.let { Country.byCode(it) })

        EntityType.PERSON -> Person(
            iri = this.iri(),
            name = this.name,
            source = DataSource.OpenOwnership,
            dateOfBirth = this.json.string("birthDate"),
            nationalities = this.nationalities.map { Country.byCode(it)!! })

        else -> throw IllegalStateException("Statement ${this.id} is not a legal entity or a person")
    }
}

internal fun BodsStatement.toRelationship(): Relationship {
    if (this.statementType != BodsStatementType.OWNERSHIP_CTRL) {
        throw IllegalStateException("Statement ${this.id} is not an ownership/control statement")
    }
    return Relationship(
        parentId = BodsRdf.RESOURCE.iri(this.interestedPartyId!!).toString(),
        childId = BodsRdf.RESOURCE.iri(this.subjectId!!).toString(),
        details = this.interestDetails()
    )
}

internal val EntityType.bodsType: BodsStatementType
    get() {
        return if (this == EntityType.LEGAL_ENTITY) BodsStatementType.ENTITY else BodsStatementType.PERSON
    }

internal fun fromOpenSanctionsRecord(record: JsonData): Entity {
    val json = record.toJson().toString().toKlaxonJson()
    val iri = FTM.iri(json.string("id")!!)
    val name = json.string("caption")!!
    return when (json.entityType) {
        EntityType.LEGAL_ENTITY -> LegalEntity(iri, name, DataSource.OpenSanctions, null, null)
        EntityType.PERSON -> Person(iri, name, DataSource.OpenSanctions, null, emptyList())
        else -> throw IllegalStateException("Statement ${iri} is not a legal entity or a person")
    }
}

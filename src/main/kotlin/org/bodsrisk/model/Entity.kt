package org.bodsrisk.model

import co.elastic.clients.json.JsonData
import org.bodsrisk.data.openownership.interestDetails
import org.bodsrisk.data.opensanctions.entityType
import org.bodsrisk.model.graph.Relationship
import org.bodsrisk.rdf.vocabulary.FTM
import org.bodsrisk.utils.toKlaxonJson
import org.eclipse.rdf4j.model.IRI
import org.kbods.rdf.BodsRdf
import org.kbods.rdf.iri
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType
import org.rdf4k.iri

class Entity(
    val iri: IRI,
    val name: String,
    val type: EntityType,
    val source: DataSource
) {
    val id: String = iri.toString()
    val isPerson: Boolean = type == EntityType.PERSON
    val isLegalEntity: Boolean = type == EntityType.LEGAL_ENTITY
}

internal val BodsStatementType.entityType: EntityType
    get() {
        return when (this) {
            BodsStatementType.ENTITY -> EntityType.LEGAL_ENTITY
            BodsStatementType.PERSON -> EntityType.PERSON
            else -> throw IllegalStateException("Statement is not an person or entity")
        }
    }

internal fun BodsStatement.toEntity(): Entity {
    return Entity(
        iri = this.iri(),
        name = this.name,
        type = this.statementType.entityType,
        source = DataSource.OpenOwnership
    )
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
    return Entity(
        iri = FTM.iri(json.string("id")!!),
        name = json.string("caption")!!,
        type = json.entityType,
        source = DataSource.OpenSanctions
    )
}

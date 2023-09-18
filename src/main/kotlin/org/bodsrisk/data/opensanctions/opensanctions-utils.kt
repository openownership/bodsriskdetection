package org.bodsrisk.data.opensanctions

import com.beust.klaxon.JsonObject
import org.bodsrisk.model.EntityType
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.rdf4k.add
import org.rdf4k.literal
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("org.bodsrisk.data.opensanctions")

val JsonObject.entityType: EntityType
    get() {
        val schema = string("schema")
        return if (schema == "Person") {
            EntityType.PERSON
        } else {
            EntityType.LEGAL_ENTITY
        }
    }

val JsonObject.openSanctionsId: String get() = string("id")!!
val JsonObject.schema: String get() = string("schema")!!
val JsonObject.properties: JsonObject get() = obj("properties")!!
val JsonObject.referents: List<String> get() = array("referents")!!
val JsonObject.openSanctionsIri: IRI get() = FTM.iri(openSanctionsId)
val JsonObject.topics: List<String> get() = properties.array("topics") ?: emptyList()
val JsonObject.addressEntity: List<String> get() = properties.array("addressEntity") ?: emptyList()

private val rdfHandlers = mapOf(
    "Family" to relationshipHandler(
        "relative",
        FTM.Family,
        FTM.Family_person,
        FTM.Family_relative,
        FTM.Family_relationship
    ),
    "Associate" to relationshipHandler(
        "associate",
        FTM.Associate,
        FTM.Associate_person,
        FTM.Associate_associate,
        FTM.Associate_relationship
    ),
    "Company" to companyHandler,
    "Address" to addressHandler
)

private val SCHEMAS_FOR_REFERENTS = setOf("Company", "Person")

fun JsonObject.toRdf(): List<Statement> {
    try {
        val statements = mutableListOf<Statement>()

        // Type-specific RDF statements
        statements.addAll(rdfHandlers[schema]?.invoke(this) ?: emptyList())

        // Add referents (linked IDs)
        if (schema in SCHEMAS_FOR_REFERENTS) {
            referents.forEach { referent ->
                val referentIri = FTM.iri(referent)
                statements.add(openSanctionsIri.sameAs(referentIri))
            }
        }

        // Add risks
        topics.forEach { topic ->
            statements.add(openSanctionsIri, BodsRisk.PROP_HAS_RISK, topic.literal())
        }

        // Add registered address if present
        addressEntity.forEach { addressId ->
            statements.add(openSanctionsIri, BodsRisk.PROP_REG_ADDRESS, FTM.iri(addressId))
        }

        return statements
    } catch (e: Exception) {
        log.error("Error processing record ${this.toJsonString()}", e)
        return emptyList()
    }
}


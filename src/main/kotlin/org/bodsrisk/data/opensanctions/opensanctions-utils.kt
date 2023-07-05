package org.bodsrisk.data.opensanctions

import com.beust.klaxon.JsonObject
import org.bodsrisk.model.EntityType
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.rdf4k.add
import org.rdf4k.literal

val JsonObject.entityType: EntityType
    get() {
        val schema = string("schema")
        return if (schema == "Person") {
            EntityType.PERSON
        } else {
            EntityType.LEGAL_ENTITY
        }
    }

val JsonObject.id: String get() = string("id")!!
val JsonObject.schema: String get() = string("schema")!!
val JsonObject.properties: JsonObject get() = obj("properties")!!
val JsonObject.referents: List<String> get() = array("referents")!!
val JsonObject.iri: IRI get() = FTM.iri(id)
val JsonObject.topics: List<String> get() = properties.array("topics") ?: emptyList()


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
    "Company" to companyHandler
)

fun JsonObject.toRdf(): List<Statement> {
    val statements = mutableListOf<Statement>()

    // Type-specific RDF statements
    statements.addAll(rdfHandlers[schema]?.invoke(this) ?: emptyList())

    // Add referents (linked IDs)
    referents.forEach { referent ->
        val referentIri = FTM.iri(referent)
        statements.add(iri.sameAs(referentIri))
    }

    // Add topics
    topics.forEach { topic ->
        statements.add(iri, FTM.Thing_topics, topic.literal())
    }

    return statements
}


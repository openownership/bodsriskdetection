package org.bodsrisk.data.opensanctions

import com.beust.klaxon.JsonObject
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.statements
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.rdf4k.literal

typealias FtmRdfHandler = (JsonObject) -> List<Statement>

fun relationshipHandler(
    relatedPersonField: String,
    rdfType: IRI,
    propPerson: IRI,
    propRelative: IRI,
    propRelationship: IRI
): FtmRdfHandler {
    return { json ->
        val statements = mutableListOf<Statement>()
        val props = json.properties
        val relationship = props.array<String>("relationship")!!.first()
        props.array<String>("person")!!.forEach { personId ->
            props.array<String>(relatedPersonField)!!
                .filter { it != personId }
                .forEach { relatedId ->
                    statements.addAll(
                        json.iri.statements(
                            RDF.TYPE to rdfType,
                            propPerson to FTM.iri(personId),
                            propRelative to FTM.iri(relatedId),
                            propRelationship to relationship.literal(),
                        )
                    )
                }
        }
        statements
    }
}

val companyHandler: FtmRdfHandler = { json ->
    val statements = mutableListOf<Statement>()
    val regCountry = json.properties.array<String>("country")?.firstOrNull()
    val regno = json.properties.array<String>("registrationNumber")?.firstOrNull()
    if (regCountry?.uppercase() == "GB" && regno != null) {
        statements.add(json.iri.sameAs(BodsRisk.company(regno)))
    }
    statements
}

package org.bodsrisk.data.opensanctions

import com.beust.klaxon.JsonObject
import io.slink.string.removeWhitespace
import org.bodsrisk.model.Address
import org.bodsrisk.rdf.sameAs
import org.bodsrisk.rdf.statements
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.rdf4k.literal
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("org.bodsrisk.data.opensanctions")

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
                        json.openSanctionsIri.statements(
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
    val regno = json.properties.array<String>("registrationNumber")?.firstOrNull()?.removeWhitespace()
    if (regCountry?.uppercase() == "GB" && regno != null) {
        statements.add(json.openSanctionsIri.sameAs(BodsRisk.company(regno)))
    }
    statements
}

val JsonObject.asAddress: Address?
    get() {
        return properties.array<String>("country")?.first()
            ?.let { country ->
                Address(
                    poBox = properties.array<String>("postOfficeBox")?.first(),
                    addressLine1 = properties.array<String>("street")?.first(),
                    addressLine2 = properties.array<String>("street2")?.first(),
                    city = properties.array<String>("city")?.first(),
                    postCode = properties.array<String>("postalCode")?.first(),
                    region = properties.array<String>("region")?.first(),
                    countryCode = country
                )
            }
    }

val addressHandler: FtmRdfHandler = { json ->
    val address = json.asAddress
    if (address != null) {
        BodsRisk.addressStatements(json.openSanctionsIri, address.full)
    } else if (json.properties.containsKey("full")) {
        BodsRisk.addressStatements(json.openSanctionsIri, json.properties.array<String>("full")?.first()!!)
    } else {
        log.info("No address data found for Address record ${json.openSanctionsId}")
        emptyList()
    }
}

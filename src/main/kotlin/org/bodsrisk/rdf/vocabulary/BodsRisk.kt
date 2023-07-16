package org.bodsrisk.rdf.vocabulary

import io.slink.id.uuid
import org.bodsrisk.model.Address
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.rdf4k.iri
import org.rdf4k.literal
import org.rdf4k.namespace
import org.rdf4k.statement

object BodsRisk {

    fun company(companyNumber: String): IRI {
        return NAMESPACE_COH.iri(companyNumber)
    }

    fun entity(id: String): IRI {
        return NAMESPACE.iri("entity/$id")
    }

    fun awardedPublicContract(companyNumber: String, contractId: String): Statement {
        return statement(company(companyNumber), PROP_AWARDED_PUBLIC_CONTRACT, entity(contractId))
    }

    fun registeredAddress(target: IRI, fullAddress: String): List<Statement> {
        val addressId = "address-" + uuid()
        val addressEntity = entity(addressId)
        return listOf(statement(target, PROP_REG_ADDRESS, addressEntity))
            .plus(addressStatements(addressEntity, fullAddress))
    }

    fun addressStatements(address: IRI, fullAddress: String): List<Statement> {
        return listOf(
            statement(address, RDF.TYPE, ADDRESS),
            statement(address, PROP_FULL_ADDRESS, fullAddress.literal()),
        )
    }

    fun registeredAddress(target: IRI, address: Address): List<Statement> {
        return registeredAddress(target, address.full)
    }

    private val NAMESPACE = "http://bods.openownership.org/risk/".namespace("bodsrisk")
    private val NAMESPACE_COH = "http://business.data.gov.uk/id/company/".namespace("ch")

    // TODO: These should suffice for this PoC, but we probably want to use something like this at some point: https://github.com/OP-TED/ePO
    val PROP_AWARDED_PUBLIC_CONTRACT = NAMESPACE.iri("awardedPublicContract")

    val ADDRESS = NAMESPACE.iri("Address")
    val PROP_REG_ADDRESS = NAMESPACE.iri("registeredAddress")
    val PROP_FULL_ADDRESS = NAMESPACE.iri("fullAddress")
}


package org.bodsrisk.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.rdf4k.iri
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

    private val NAMESPACE = "http://bods.openownership.org/risk/".namespace("bodsrisk")
    private val NAMESPACE_COH = "http://business.data.gov.uk/id/company/".namespace("ch")

    // TODO: These should suffice for this PoC, but we probably want to use something like this at some point: https://github.com/OP-TED/ePO
    private val PROP_AWARDED_PUBLIC_CONTRACT = NAMESPACE.iri("awardedPublicContract")
}


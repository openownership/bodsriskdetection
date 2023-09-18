package org.bodsrisk.rdf.vocabulary

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.bodsrisk.model.Address
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.rdf4k.literal
import org.rdf4k.statement

class BodsRiskVocabularyTest : StringSpec({

    "address" {
        val target = BodsRisk.entity("1")
        val address = Address(addressLine1 = "2 Street", postCode = "AB12 XYZ", countryCode = "GB")
        val statements = BodsRisk.registeredAddress(target, address)
        statements.size shouldBe 3
        val addressObject =
            statements.find { it.subject == target && it.predicate == BodsRisk.PROP_REG_ADDRESS }!!.`object` as IRI
        val addressStatements = statements.filter { it.subject == addressObject }
        addressStatements shouldContain statement(addressObject, RDF.TYPE, BodsRisk.ADDRESS)
        addressStatements shouldContain statement(addressObject, BodsRisk.PROP_FULL_ADDRESS, address.full.literal())
    }

    "address string" {
        val target = BodsRisk.entity("1")
        val fullAddress = "My company's address"
        val statements = BodsRisk.registeredAddress(target, fullAddress)
        statements.size shouldBe 3
        val addressObject =
            statements.find { it.subject == target && it.predicate == BodsRisk.PROP_REG_ADDRESS }!!.`object` as IRI
        val addressStatements = statements.filter { it.subject == addressObject }
        addressStatements shouldContain statement(addressObject, RDF.TYPE, BodsRisk.ADDRESS)
        addressStatements shouldContain statement(addressObject, BodsRisk.PROP_FULL_ADDRESS, fullAddress.literal())
    }
})

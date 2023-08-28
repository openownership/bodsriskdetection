package org.bodsrisk.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bodsrisk.data.cleanCompanyName
import org.bodsrisk.data.companiesHouseNumber

class CompaniesUtilsTest : StringSpec({

    "companiesHouseNumber" {
        "12345".companiesHouseNumber() shouldBe "00012345"
        "12345678".companiesHouseNumber() shouldBe "12345678"
        "NI3456".companiesHouseNumber() shouldBe "NI3456"
    }

    "cleanCompanyName" {
        "My Space   Limited".cleanCompanyName() shouldBe "MY SPACE LIMITED"
        "Biffa Waste Services LTd".cleanCompanyName() shouldBe "BIFFA WASTE SERVICES LIMITED"
        "Biffa Waste Services LTd.".cleanCompanyName() shouldBe "BIFFA WASTE SERVICES LIMITED"
        "PwC LLp. ".cleanCompanyName() shouldBe "PWC LLP"
    }
})

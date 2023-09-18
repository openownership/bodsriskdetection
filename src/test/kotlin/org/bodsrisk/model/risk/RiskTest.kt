package org.bodsrisk.model.risk

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RiskTest : StringSpec({

    "riskId" {
        "Crime".riskId() shouldBe "crime"
        "crime.Financial-crime".riskId() shouldBe "crime-financial-crime"
        "Politically Exposed".riskId() shouldBe "politically-exposed"
    }

    "risk level" {
        RiskLevel.level("crime-traffick-drug") shouldBe RiskLevel.RED
        RiskLevel.level("crime.traffick-drug") shouldBe RiskLevel.RED
        RiskLevel.level("Crime Traffick Drug") shouldBe RiskLevel.RED
        RiskLevel.level("bank") shouldBe RiskLevel.AMBER
        RiskLevel.level("Pep") shouldBe RiskLevel.AMBER
    }
})
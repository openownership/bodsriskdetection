package org.bodsrisk.data

import io.kotest.core.spec.style.StringSpec
import org.bodsrisk.data.openownership.OpenOwnershipDataset
import org.kbods.read.BodsStatement

class OpenOwnershipDatasetTest : StringSpec({

    "registered address" {
        val json = """
            {
                "statementID": "openownership-register-13690228758630993958",
                "statementType": "entityStatement",
                "entityType": "registeredEntity",
                "name": "BRASS NO.8 MORTGAGE HOLDINGS LIMITED",
                "incorporatedInJurisdiction": {
                    "name": "United Kingdom of Great Britain and Northern Ireland",
                    "code": "GB"
                },
                "identifiers": [
                    {
                        "scheme": "GB-COH",
                        "schemeName": "Companies House",
                        "id": "11996791"
                    },
                    {
                        "schemeName": "OpenCorporates",
                        "id": "https://opencorporates.com/companies/gb/11996791",
                        "uri": "https://opencorporates.com/companies/gb/11996791"
                    },
                    {
                        "schemeName": "GB Persons Of Significant Control Register",
                        "id": "/company/11996873/persons-with-significant-control/corporate-entity/-CEgYnbiLeeAsQdInlT_JNPvvJE"
                    },
                    {
                        "schemeName": "OpenOwnership Register",
                        "id": "http://register.openownership.org/entities/5ce6ff0f9dfc3fae181a75df",
                        "uri": "http://register.openownership.org/entities/5ce6ff0f9dfc3fae181a75df"
                    }
                ],
                "foundingDate": "2019-05-15",
                "addresses": [
                    {
                        "type": "registered",
                        "address": "C/O Wilmington Trust Sp Services (London) Limited, Third Floor, 1 King's Arms Yard, London, EC2R 7AF",
                        "country": "GB"
                    }
                ]
            }
        """
        val statement = BodsStatement(json)
        val statements = OpenOwnershipDataset.registeredAddressStatements(statement)
        statements.forEach {
            println(it)
        }
    }
})

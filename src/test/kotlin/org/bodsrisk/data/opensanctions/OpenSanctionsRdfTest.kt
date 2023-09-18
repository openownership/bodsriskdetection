package org.bodsrisk.data.opensanctions

import io.kotest.core.spec.style.StringSpec
import org.bodsrisk.utils.toKlaxonJson

class OpenSanctionsRdfTest : StringSpec({

    "family relationships" {
        val jsonString = """
            {
                "id": "rupep-q2288379-family-q503147",
                "caption": "Family",
                "schema": "Family",
                "properties": {
                    "relative": [
                        "Q503147"
                    ],
                    "relationship": [
                        "wife",
                        "husband"
                    ],
                    "person": [
                        "Q2288379"
                    ]
                },
                "referents": [],
                "datasets": [
                    "ru_rupep"
                ],
                "first_seen": "2023-04-20T09:48:23",
                "last_seen": "2023-07-11T12:47:30",
                "target": false,
                "last_change": "2023-04-20T09:48:23"
            }
        """.trimIndent()
        val json = jsonString.toKlaxonJson()
        json.toRdf().forEach {
            println(it)
        }
    }

})
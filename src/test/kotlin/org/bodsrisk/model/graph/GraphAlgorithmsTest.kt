package org.bodsrisk.model.graph

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class GraphAlgorithmsTest : StringSpec() {

    init {
        "allPaths - single path" {
            relationships("A" to "B", "B" to "C", "C" to "D")
                .allPaths("A", "D") shouldBe listOf(relationships("A" to "B", "B" to "C", "C" to "D"))
        }

        "allPaths - multiple paths" {
            relationships(
                "A" to "B",
                "B" to "C",
                "C" to "D",
                "B" to "D"
            ).allPaths("A", "D") shouldContainExactly listOf(
                relationships("A" to "B", "B" to "C", "C" to "D"),
                relationships("A" to "B", "B" to "D")
            )

            relationships(
                "A" to "B",
                "B" to "C",
                "C" to "D",
                "D" to "E",
                "E" to "F",
                "A" to "C"
            ).allPaths("A", "F") shouldContainExactly listOf(
                relationships("A" to "B", "B" to "C", "C" to "D", "D" to "E", "E" to "F"),
                relationships("A" to "C", "C" to "D", "D" to "E", "E" to "F")
            )
        }

        "allPaths - no paths found" {
            relationships(
                "A" to "B",
                "B" to "C",
                "C" to "D",
                "F" to "G"
            ).allPaths("A", "G") shouldBe emptyList()
        }

        "shortestPath - singlePath" {
            val relationships = relationships("A" to "B", "B" to "C", "C" to "D")
            relationships.shortestPath("A", "D") shouldBe relationships("A" to "B", "B" to "C", "C" to "D")
            relationships.shortestPath("A", "C") shouldBe relationships("A" to "B", "B" to "C")
            relationships.shortestPath("B", "D") shouldBe relationships("B" to "C", "C" to "D")
            relationships.shortestPath("A", "B") shouldBe relationships("A" to "B")
            relationships.shortestPath("B", "C") shouldBe relationships("B" to "C")
        }

        "shortestPath - multiplePaths" {
            val relationships = relationships(
                "A" to "B",
                "B" to "C",
                "C" to "D",
                "D" to "E",
                "E" to "F",
                "A" to "C"
            )
            relationships.shortestPath("A", "F") shouldBe relationships("A" to "C", "C" to "D", "D" to "E", "E" to "F")
        }
    }

    private fun relationships(vararg pairs: Pair<String, String>): List<Relationship> {
        return pairs.map { Relationship(it.first, it.second, emptyList()) }
    }
}
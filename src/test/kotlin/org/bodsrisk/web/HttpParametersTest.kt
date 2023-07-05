@file:Suppress("UNCHECKED_CAST")

package org.bodsrisk.web

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.core.convert.DefaultConversionService
import io.micronaut.http.simple.SimpleHttpParameters

class HttpParametersTest : StringSpec({

    val conversionService = DefaultConversionService()

    fun Map<String, List<String>>.shouldBe(expected: String) {
        SimpleHttpParameters(this as Map<CharSequence, List<String>>, conversionService).queryString() shouldBe expected
    }

    fun Map<String, List<String>>.addShouldBe(param: String, value: String?, expected: String) {
        SimpleHttpParameters(this as Map<CharSequence, List<String>>, conversionService).addParam(
            param,
            value
        ) shouldBe expected
    }

    fun Map<String, List<String>>.addShouldBe(params: Map<String, Any?>, expected: String) {
        SimpleHttpParameters(
            this as Map<CharSequence, List<String>>,
            conversionService
        ).addParams(params) shouldBe expected
    }

    "query string" {
        mapOf("p" to listOf("value")).shouldBe("p=value")
        mapOf("p" to listOf("value1", "value2")).shouldBe("p=value1&p=value2")
        mapOf("a" to listOf("true"), "b" to listOf("ENTITY")).shouldBe("a=true&b=ENTITY")
        mapOf("name" to listOf("John Smith")).shouldBe("name=John+Smith")
    }

    "add param" {
        mapOf("p" to listOf("value")).addShouldBe("p", "v2", "p=v2")
        mapOf("a" to listOf("true"), "b" to listOf("ENTITY")).addShouldBe("b", "TYPE", "a=true&b=TYPE")
        mapOf("a" to listOf("true"), "b" to listOf("ENTITY")).addShouldBe("b", null, "a=true")
        mapOf("a" to listOf("true")).addShouldBe("b", "TYPE", "a=true&b=TYPE")
        mapOf("a" to listOf("true")).addShouldBe("a", null, "")
        emptyMap<String, List<String>>().addShouldBe("b", "TYPE", "b=TYPE")
    }

    "add params" {
        mapOf("p" to listOf("value")).addShouldBe(mapOf("p" to "v2"), "p=v2")
        mapOf("entity" to listOf("COMPANY"), "from" to listOf("10"), "to" to listOf("20")).addShouldBe(
            mapOf(
                "from" to "100",
                "to" to "200"
            ), "entity=COMPANY&from=100&to=200"
        )
    }
})


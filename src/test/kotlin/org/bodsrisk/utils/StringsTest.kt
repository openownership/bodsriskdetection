package org.bodsrisk.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StringsTest : StringSpec({

    "tokenize" {
        "My string".tokenize() shouldBe listOf("My", "string")
        "A ,;complex - string   \n with \t > some ( weird Chars!".tokenize() shouldBe listOf(
            "A",
            "complex",
            "string",
            "with",
            "some",
            "weird",
            "Chars"
        )
    }
})
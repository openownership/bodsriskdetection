package org.bodsrisk.model

import io.slink.string.cleanWhitespace

data class Address(
    val poBox: String? = null,
    val premises: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val postCode: String? = null,
    val region: String? = null,
    val country: String? = null,
    val careOf: String? = null
) {

    val hash: String by lazy {
        listOfNotNull(
            poBox,
            addressLine1,
            addressLine2,
            city,
            postCode?.replace("\\s+".toRegex(), ""),
            region,
            country
        )
            .map { it.uppercase().cleanWhitespace() }
            .joinToString { " " }
    }
}

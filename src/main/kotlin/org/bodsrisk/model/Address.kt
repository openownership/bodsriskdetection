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
    val countryCode: String? = null,
    val careOf: String? = null
) {

    val full: String by lazy {
        val full = listOfNotNull(
            poBox?.cleanWhitespace(),
            addressLine1?.cleanWhitespace(),
            addressLine2?.cleanWhitespace(),
            city?.cleanWhitespace(),
            region?.cleanWhitespace(),
            postCode?.cleanWhitespace(),
        ).joinToString(" ")
        cleanFullAddress(full, countryCode!!)
    }

    companion object {
        fun cleanFullAddress(address: String, countryCode: String): String {
            return ("$address $countryCode")
                .replace(",", " ")
                .cleanWhitespace()
                .uppercase()
        }
    }
}

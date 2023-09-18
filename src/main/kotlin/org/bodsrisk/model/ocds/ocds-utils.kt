package org.bodsrisk.model.ocds

import org.bodsrisk.data.companiesHouseNumber

private const val SCHEMA_PREFIX_COH = "GB-COH-"

fun String.ocdsCompanyNumber(): String? {
    return if (this.startsWith(SCHEMA_PREFIX_COH)) {
        this.replace(SCHEMA_PREFIX_COH, "").companiesHouseNumber()
    } else {
        null
    }
}
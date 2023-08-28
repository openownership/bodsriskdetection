package org.bodsrisk.data

import io.slink.string.REGEX_NON_ALPHA
import io.slink.string.cleanWhitespace
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("org.bodsrisk.data.utils")

private val REGEX_PSP = "/company/(?<companyNumber>.+)/persons-with-significant-control/(.+)/(?<pscRef>.+)".toRegex()

fun String.pscRefSlug(): String? {
    val match = REGEX_PSP.matchEntire(this)
    return if (match != null) {
        val groups = match.groups
        val companyNumber = groups["companyNumber"]!!.value
        val pscRef = groups["pscRef"]!!.value
        "gb-coh-psc-$companyNumber-$pscRef".lowercase().replace("_", "-")
    } else {
        log.warn("Could not parse PSP reference '$this'")
        null
    }
}

val companyNameReplace = mapOf(
    "LTD" to "LIMITED",
).map {
    """(\s+)${it.key}(\.|\s+|$)""".toRegex() to " ${it.value} "
}.toMap()

val companyNameCleanse = listOf(
    "CORP",
    "GMBH",
    "INC",
    "INCORPORATED",
    "LIMITED",
    "LLC",
    "LLLP",
    "LLP",
    "LP",
    "LTD",
    "OOO",
    "PLC",
    "PLLC",
    "SA",
    "SARL",
    "SRL",
    "TRUST"
).associate {
    """(\s+)${it}(\.)""".toRegex() to " $it "
}

fun String.cleanCompanyName(): String {
    var cleanName = this.uppercase()
    companyNameReplace.forEach {
        cleanName = cleanName.replace(it.key, it.value)
    }
    companyNameCleanse.forEach {
        cleanName = cleanName.replace(it.key, it.value)
    }
    return cleanName.cleanWhitespace()
}

private val REGEX_LETTERS = "[A-Z]".toRegex()

fun String.companiesHouseNumber(): String {
    var cleanNumber = this
        .replace(REGEX_NON_ALPHA, "")
        .trim()
        .uppercase()
    if (!cleanNumber.contains(REGEX_LETTERS)) {
        cleanNumber = cleanNumber.padStart(8, '0')
    }
    return cleanNumber
}

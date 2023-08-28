package org.bodsrisk.data.openownership

import io.slink.string.titleCaseFirstChar
import org.kbods.read.BodsStatement

fun BodsStatement.pscRefs(): List<String> {
    return this.identifiers(BodsIdSchemes.UK_PSC)
}

fun BodsStatement.interestDetails(): List<String> {
    return interests
        .filter { it.string("details") != null }
        .map {
            it.string("details")!!
                .replace("-", " ")
                .replace("(\\d+) to (\\d+) percent".toRegex(), "$1-$2%")
                .titleCaseFirstChar()
        }
}

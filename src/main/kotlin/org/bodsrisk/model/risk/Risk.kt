package org.bodsrisk.model.risk

import org.bodsrisk.utils.i18n.I18n

typealias Risks = List<Risk>

data class Risk(
    val id: String,
    val label: String,
    val level: RiskLevel
) {

    constructor(risk: String) : this(
        id = risk.riskId(),
        label = I18n.get("risk.${risk}"),
        level = RiskLevel.level(risk)
    )
}

fun List<Risk>.sort(): List<Risk> {
    return this.sortedByDescending { it.level.factor }
}

private val REGEX_RISK_ID_REPLACE = "[ _.]".toRegex()

internal fun String.riskId(): String {
    return this.lowercase().replace(REGEX_RISK_ID_REPLACE, "-")
}

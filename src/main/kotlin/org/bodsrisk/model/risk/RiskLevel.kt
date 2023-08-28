package org.bodsrisk.model.risk

enum class RiskLevel(val factor: Int) {
    RED(2),
    AMBER(1);

    companion object {

        // TODO: This would normally go in a config
        private val redRisk = setOf(
            "crime",
            "crime-fraud",
            "crime-cyber",
            "crime-fin",
            "crime-theft",
            "crime-war",
            "crime-boss",
            "crime-terror",
            "crime-traffick",
            "crime-traffick-drug",
            "crime-traffick-human",
            "role-pep",
            "role-spy",
            "role-oligarch",
            "role-act",
            "asset-frozen",
            "sanction",
            "debarment",
            "poi",
            "icij",
            "panama-papers"
        )

        fun level(risk: String): RiskLevel {
            return if (risk.riskId() in redRisk) {
                RED
            } else {
                AMBER
            }
        }
    }
}

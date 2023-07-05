package org.bodsrisk.model.risk

import org.bodsrisk.data.opensanctions.FtmTopic

enum class RiskLevel(val factor: Int) {
    RED(2),
    AMBER(1);

    companion object {
        private val ftmTopicRed = setOf(
            FtmTopic.CRIME,
            FtmTopic.CRIME_FRAUD,
            FtmTopic.CRIME_CYBER,
            FtmTopic.CRIME_FIN,
            FtmTopic.CRIME_THEFT,
            FtmTopic.CRIME_WAR,
            FtmTopic.CRIME_BOSS,
            FtmTopic.CRIME_TERROR,
            FtmTopic.CRIME_TRAFFICK,
            FtmTopic.CRIME_TRAFFICK_DRUG,
            FtmTopic.CRIME_TRAFFICK_HUMAN,
            FtmTopic.ROLE_PEP,
            FtmTopic.ROLE_SPY,
            FtmTopic.ROLE_OLIGARCH,
            FtmTopic.ROLE_ACT,
            FtmTopic.ASSET_FROZEN,
            FtmTopic.SANCTION,
            FtmTopic.DEBARMENT,
            FtmTopic.POI
        )
        private val ftmTopicRiskLevels: Map<FtmTopic, RiskLevel> = FtmTopic.values().associateWith { topic ->
            if (topic in ftmTopicRed) RED else AMBER
        }

        fun ftmTopicRiskLevel(topic: FtmTopic): RiskLevel {
            return ftmTopicRiskLevels[topic]!!
        }
    }
}
package org.bodsrisk.model.risk

import org.bodsrisk.data.opensanctions.FtmTopic

typealias Risks = List<Risk>

data class Risk(
    val name: String,
    val label: String,
    val level: RiskLevel
) {

    constructor(ftmTopic: String) : this(FtmTopic.fromValue(ftmTopic))

    constructor(topic: FtmTopic) : this(
        name = topic.value,
        label = topic.label,
        level = RiskLevel.ftmTopicRiskLevel(topic)
    )
}

fun List<Risk>.sort(): List<Risk> {
    return this.sortedByDescending { it.level.factor }
}
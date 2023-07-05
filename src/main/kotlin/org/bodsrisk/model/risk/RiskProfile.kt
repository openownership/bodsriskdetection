package org.bodsrisk.model.risk

import org.bodsrisk.utils.toKlaxonJson

data class RiskProfile(
    val id: String,
    val risks: List<Risk>,
    val notes: List<String>
) {

    companion object {
        fun fromJson(jsonString: String): RiskProfile {
            val json = jsonString.toKlaxonJson()
            val properties = json.obj("properties")!!
            return RiskProfile(
                id = json.string("id")!!,
                risks = properties.array<String>("topics")
                    ?.map { Risk(it) }
                    ?: emptyList(),
                notes = properties.array("notes") ?: emptyList()
            )
        }
    }
}
package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwardValue(
    val amount: Double,
    val currency: String
)
package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Award(
    val suppliers: List<Supplier>,
    val value: AwardValue
)
package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tender(
    val title: String?,
    val description: String?,
)
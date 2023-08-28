package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Buyer(
    val id: String,
    val name: String
)
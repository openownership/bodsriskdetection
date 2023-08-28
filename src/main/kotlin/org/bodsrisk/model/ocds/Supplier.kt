package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Supplier(
    val id: String,
    val name: String?
) {

    @JsonIgnore
    val registrationNumber: String? = id.ocdsCompanyNumber()
}
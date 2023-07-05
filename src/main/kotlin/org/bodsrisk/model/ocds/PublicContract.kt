package org.bodsrisk.model.ocds

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicContract(
    val ocid: String,
    val date: String,
    val tender: Tender,
    val buyer: Buyer,
    val awards: List<Award>,

    // We use the OCID as the ID here because these records are sometimes updates
    // to existing records with the same OCID and we're not interested in the old ones
    // for this PoC
    val id: String = ocid
) {

    @JsonIgnore
    val value: Double = awards.sumOf { it.value.amount }

    @JsonIgnore
    val suppliers: List<Supplier> = awards.flatMap { it.suppliers }
}

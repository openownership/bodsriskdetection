package org.bodsrisk.model

import org.bodsrisk.model.ocds.PublicContract

data class PublicContracts(val contracts: List<PublicContract>) {
    val totalValue: Double = contracts.sumOf { it.value }
}
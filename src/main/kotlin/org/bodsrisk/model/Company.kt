package org.bodsrisk.model

data class Company(
    val name: String,
    val companyNumber: String,
    val registeredAddress: Address,
    val previousNames: List<String>
) {
    val allNames: List<String> by lazy {
        previousNames.plus(name)
    }
}
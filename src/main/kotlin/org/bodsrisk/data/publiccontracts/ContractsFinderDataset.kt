package org.bodsrisk.data.publiccontracts

import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import com.fasterxml.jackson.annotation.JsonProperty
import io.slink.string.cleanWhitespace
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVPrinter
import org.bodsrisk.data.importer.*
import org.bodsrisk.data.sameasdb.SameAsRecord
import org.bodsrisk.elasticsearch.indexExists
import org.bodsrisk.model.ocds.PublicContract
import org.bodsrisk.model.ocds.Supplier
import org.bodsrisk.rdf.vocabulary.BodsRisk
import org.bodsrisk.utils.*
import org.rdf4k.StatementsBatch
import java.io.File
import java.io.FileWriter

@Singleton
class ContractsFinderDataset(
    private val esIndices: ElasticsearchIndicesClient,
) : DataImporter {

    private val source = FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.ZIP)
    override val requiresImport: Boolean = !esIndices.indexExists(INDEX)

    override fun buildTask(task: ImportTask) {
        task.source(source)
            .importRdf { file, rdfBatch ->
                file.forEachLine { jsonString ->
                    val contract = jsonString.toPojo(PublicContract::class)
                    addRdfStatements(contract, rdfBatch)
                }
            }.index(INDEX) { file, docBatch ->
                file.forEachLine { jsonString ->
                    val contract = jsonString.toPojo(PublicContract::class)
                    docBatch.add(jsonString, contract.id)
                }
            }
    }

    private fun addRdfStatements(contract: PublicContract, rdfBatch: StatementsBatch) {
        contract.suppliers
            .filter { it.id.isNotBlank() }
            .forEach { supplier ->
                val supplierId = supplier.id.replace(REGEX_SUPPLIER_ID, "")
                rdfBatch.add(BodsRisk.awardedPublicContract(supplierId, contract.id))
                supplier.registrationNumber?.let { regno ->
                    rdfBatch.add(BodsRisk.awardedPublicContract(regno, contract.id))
                }
            }
    }

    internal fun generateSameAsRecords() {
        val companyRefs = File("data/public-contracts/supplier-names.csv").readCsv<CompanyRef>()
        val sameAsRecords = mutableMapOf<String, SameAsRecord>()
        source.forEachFile { file ->
            file.forEachLine { jsonString ->
                val contract = jsonString.toPojo(PublicContract::class)
                contract.suppliers
                    .filter { it.name != null }
                    .forEach { supplier ->
                        val cleanName = supplier.name!!.cleanWhitespace().uppercase()
                        companyRefs.firstOrNull { cleanName.startsWith(it.cleanPrefix) }
                            ?.let {
                                val regno = it.companyNumber
                                sameAsRecords.putIfAbsent(
                                    regno,
                                    SameAsRecord(BodsRisk.company(regno).toString(), "gb-contracts-finder")
                                )
                                sameAsRecords[regno]!!
                                    .addId(BodsRisk.company(supplier.id).toString())
                                    .addReference("Matched Contracts Finder name '${it.companyNamePrefix}' against '${supplier.name}'")
                            }
                    }
            }

            File("data/same-as-db.jsonl").writeText(
                sameAsRecords.values.joinToString("\n") { it.toJsonString() }
            )
        }
    }

    internal fun generateSupplierIds() {
        val suppliers = mutableListOf<Supplier>()
        source.forEachFile { file ->
            file.forEachLine { jsonString ->
                val contract = jsonString.toPojo(PublicContract::class)
                suppliers.addAll(contract.suppliers.filter { it.name != null })
            }
        }
        val cleanSuppliers = suppliers
            .map { it.copy(id = it.id, name = it.name!!.cleanWhitespace().uppercase()) }
            .toSet()
            .sortedBy { it.name }
        CSVPrinter(
            FileWriter(File("etc/all-suppliers.csv")),
            csvPrintFormat("ID", "Name")
        ).use { printer ->
            cleanSuppliers.forEach {
                printer.printRecord(it.id, it.name)
            }
        }
    }

    companion object {
        const val INDEX = "gb-public-contracts"
        private val REGEX_SUPPLIER_ID = "[^a-zA-Z\\d-_]+".toRegex()
        private const val DOWNLOAD_URL =
            "https://bods-rdf.s3-eu-west-1.amazonaws.com/data/extras/gb-public-contracts.zip"
    }
}

internal data class CompanyRef(
    @JsonProperty("CompanyNumber")
    val companyNumber: String,
    @JsonProperty("CompanyNamePrefix")
    val companyNamePrefix: String
) {
    val cleanPrefix: String = companyNamePrefix.cleanWhitespace().uppercase()
}

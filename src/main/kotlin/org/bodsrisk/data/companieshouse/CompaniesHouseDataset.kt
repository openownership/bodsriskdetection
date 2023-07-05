package org.bodsrisk.data.companieshouse

import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVRecord
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.ImportTask
import org.bodsrisk.data.importer.Unpack
import org.bodsrisk.model.Address
import org.bodsrisk.model.Company
import org.bodsrisk.utils.forEachCsvRecord

@Singleton
class CompaniesHouseDataset(
    private val esIndices: ElasticsearchIndicesClient,
) : DataImporter {

    private val source = FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.ZIP)

    //    override val requiresImport: Boolean = !esIndices.indexExists(INDEX)
    override val requiresImport: Boolean = false

    override fun buildTask(task: ImportTask) {
        task.source(source)
            .index(INDEX) { csvFile, docBatch ->
                csvFile.forEachCsvRecord { csvRecord ->
                    val company = csvRecord.toCompany()
                    docBatch.add(company)
                }
            }
    }

    companion object {
        private const val INDEX = "gb-companies"
        private const val DOWNLOAD_URL =
            "https://download.companieshouse.gov.uk/BasicCompanyDataAsOneFile-2023-05-01.zip"
    }
}

internal fun CSVRecord.toCompany(): Company {
    return Company(
        name = this["CompanyName"],
        companyNumber = this["CompanyNumber"],
        registeredAddress = Address(
            careOf = this["RegAddress.CareOf"],
            poBox = this["RegAddress.POBox"],
            addressLine1 = this["RegAddress.AddressLine1"],
            addressLine2 = this["RegAddress.AddressLine2"],
            city = this["RegAddress.PostTown"],
            country = this["RegAddress.Country"],
            region = this["RegAddress.County"],
            postCode = this["RegAddress.PostCode"]
        ),
        previousNames = this.prevNames()
    )
}

private fun CSVRecord.prevNames(): List<String> {
    return (1..10)
        .map { "PreviousName_${it}.CompanyName" }
        .map { this[it] }
        .filter { it.isNotBlank() }
        .filterNotNull()
}

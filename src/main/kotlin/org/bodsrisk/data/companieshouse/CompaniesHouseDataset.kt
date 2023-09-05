package org.bodsrisk.data.companieshouse

//@Singleton
//class CompaniesHouseDataset : DataImporter() {
//
//    override fun createImportTask(): FileImportTask {
//        return importTask {
//            source(FileSource.Remote(DOWNLOAD_URL).unpack(Unpack.ZIP))
//            runIfIndexMissing(INDEX)
//            createIndex(INDEX)
//            indexCsv(INDEX) { ElasticsearchDocument(it.toCompany()) }
//        }
//    }
//
//    companion object {
//        private const val INDEX = "gb-companies"
//        private const val DOWNLOAD_URL =
//            "https://download.companieshouse.gov.uk/BasicCompanyDataAsOneFile-2023-05-01.zip"
//    }
//}
//
//internal fun CSVRecord.toCompany(): Company {
//    return Company(
//        name = this["CompanyName"],
//        companyNumber = this["CompanyNumber"],
//        registeredAddress = Address(
//            careOf = this["RegAddress.CareOf"],
//            poBox = this["RegAddress.POBox"],
//            addressLine1 = this["RegAddress.AddressLine1"],
//            addressLine2 = this["RegAddress.AddressLine2"],
//            city = this["RegAddress.PostTown"],
//            countryCode = this["RegAddress.Country"],
//            region = this["RegAddress.County"],
//            postCode = this["RegAddress.PostCode"]
//        ),
//        previousNames = this.prevNames()
//    )
//}
//
//private fun CSVRecord.prevNames(): List<String> {
//    return (1..10)
//        .map { "PreviousName_${it}.CompanyName" }
//        .map { this[it] }
//        .filter { it.isNotBlank() }
//        .filterNotNull()
//}

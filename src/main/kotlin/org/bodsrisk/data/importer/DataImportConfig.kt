package org.bodsrisk.data.importer

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.inject.Inject

@ConfigurationProperties("app.data-import")
data class DataImportConfig
@ConfigurationInject
constructor(
    val runAtStartup: Boolean,
    val importerNames: List<String>?
) {

    @Inject
    lateinit var importerList: List<DataImporter>

    val importers: List<DataImporter> by lazy {
        if (!importerNames.isNullOrEmpty()) {
            importerList.filter { it::class.simpleName in importerNames }
        } else {
            importerList
        }
    }
}

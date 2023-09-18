package org.bodsrisk.data.importer

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.inject.Inject

/**
 * Configures the behaviour of the [ImportService].
 * @property runAtStartup Boolean to indicate whether the importers should run at application startup.
 * @property importerNames List of importer names (simple class name for a [DataImporter] implementation) that need to run.
 * This can be used to only select specific importers to be run. Defaults to empty list (all importers are run).
 */
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

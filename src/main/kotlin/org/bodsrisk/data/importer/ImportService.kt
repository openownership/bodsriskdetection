package org.bodsrisk.data.importer

import io.micronaut.runtime.event.ApplicationStartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.slink.datetime.humanReadableString
import io.slink.files.TempDir
import io.slink.files.withTempDir
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.measureTime

/**
 * Service responsible for running the data importers
 */
@Singleton
class ImportService(private val config: DataImportConfig) {

    @EventListener
    fun importData(event: ApplicationStartupEvent) {
        if (!config.runAtStartup) {
            log.info("Data import is disabled, skipping")
            return
        }

        // We run importers sequentially, but this can be easily extended to
        // implement the running of these importers concurrently.
        // The main constraint here is the GraphDB license and the fact that ingestion is single-threaded
        // with the Free license. This makes concurrent writes essentially serialized, which means there's
        // no benefit in running multiple importers in parallel, since most of them require an RDF database write.

        withTempDir(File("temp")) { tempDir ->
            config.importers.forEach { importer ->
                runImporter(tempDir, importer)
            }
        }
    }

    private fun runImporter(tempDir: TempDir, importer: DataImporter) {
        val name = importer::class.simpleName!!
        val task = importer.createImportTask()
        if (task.shouldRun) {
            log.info("Running data importer $name")
            val duration = measureTime {
                task.run(tempDir, JOB_POOL_SIZE)
            }
            log.info("Importer $name finished in ${duration.humanReadableString()}")
        } else {
            log.info("Importer $name doesn't need to run")
        }
    }

    companion object {
        // Again, due to GraphDB limitations, there isn't value in running more than 2 threads (one for ES one for GraphDB)
        private const val JOB_POOL_SIZE = 2
        private val log = LoggerFactory.getLogger(ImportService::class.java)
    }
}


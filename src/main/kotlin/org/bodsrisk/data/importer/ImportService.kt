package org.bodsrisk.data.importer

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import io.micronaut.runtime.event.ApplicationStartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.slink.datetime.humanReadableString
import jakarta.inject.Singleton
import org.bodsrisk.utils.ThreadPool
import org.eclipse.rdf4j.repository.Repository
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Singleton
class ImportService(
    private val config: DataImportConfig,
    private val rdfRepository: Repository,
    private val esClient: ElasticsearchClient,
    private val esIndices: ElasticsearchIndicesClient,
) {

    @EventListener
    fun importData(event: ApplicationStartupEvent) {
        if (!config.runAtStartup) {
            log.info("Data import is disabled, skipping")
            return
        }

        val ranImporters = config.importers.filter { !it.requiresImport }.map { it::class }
        ThreadPool<Unit>(THREAD_POOL_SIZE).use { threadPool ->
            config.importers
                .filter { it::class !in ranImporters }
                .forEach { importer ->
                    log.info("Queueing importer ${importer::class.simpleName}")
                    threadPool.submit {
                        runImporter(importer)
                    }
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    internal fun runImporter(importer: DataImporter) {
        val name = importer::class.simpleName
        log.info("Running data importer $name")
        val importTask = ImportTask(rdfRepository, esClient, esIndices)
        try {
            importer.buildTask(importTask)
            val duration = measureTime { importTask.run() }
            log.info("Importer $name finished in ${duration.humanReadableString()}")
        } catch (e: Exception) {
            log.error("Error running importer $name", e)
            throw e
        }
    }

    companion object {
        // The main constraint here is the GraphDB license and the fact that ingestion is single-threaded
        // with the Free license. This makes concurrent writes essentially serialized, which means there's not much
        // value in having more than 1 thread
        private const val THREAD_POOL_SIZE = 1

        private val log = LoggerFactory.getLogger(ImportService::class.java)
    }
}


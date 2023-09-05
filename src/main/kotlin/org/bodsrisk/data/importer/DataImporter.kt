package org.bodsrisk.data.importer

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import jakarta.inject.Inject
import org.apache.commons.csv.CSVRecord
import org.bodsrisk.elasticsearch.ElasticsearchDocument
import org.bodsrisk.elasticsearch.forceCreateIndex
import org.bodsrisk.elasticsearch.indexExists
import org.bodsrisk.elasticsearch.withBatch
import org.bodsrisk.utils.forEachCsvRecord
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.withStatementsBatch
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Base class and utilities for data importers. The core callback here is [createImportTask] which
 * is the method that an importer needs to implement. This will return a [FileImportTask] describing the
 * importer's behaviour.
 */
abstract class DataImporter {

    @Inject
    private lateinit var rdfRepository: Repository

    @Inject
    private lateinit var esClient: ElasticsearchClient

    @Inject
    private lateinit var esIndices: ElasticsearchIndicesClient

    abstract fun createImportTask(): FileImportTask

    fun importTask(block: FileImportTask.() -> Unit): FileImportTask {
        val importTask = FileImportTask()
        block(importTask)
        return importTask
    }

    private fun FileImportTask.runIfIndexMissing(index: String) {
        runIf { !esIndices.indexExists(index) }
    }

    private fun FileImportTask.createIndex(index: String) {
        beforeStart {
            esIndices.forceCreateIndex(index)
        }
    }

    fun FileImportTask.withIndex(index: String) {
        runIfIndexMissing(index)
        createIndex(index)
    }

    fun <T> FileImportTask.index(
        index: String,
        indexBatchSize: Int = DEFAULT_INDEX_BATCH_SIZE,
        convert: (String) -> ElasticsearchDocument<T>
    ) {
        addFileConsumer { file ->
            esClient.withBatch(index, indexBatchSize) { batch ->
                readLines(file) { batch.addDocument(convert(it)) }
            }
        }
    }

    fun FileImportTask.importRdf(
        statementBatchSize: Int = DEFAULT_RDF_BATCH_SIZE,
        convert: (String) -> Collection<Statement>
    ) {
        addFileConsumer { file ->
            rdfRepository.withStatementsBatch(statementBatchSize) { rdfBatch ->
                readLines(file) { rdfBatch.add(convert(it)) }
            }
        }
    }

    fun <T> FileImportTask.indexCsv(
        index: String,
        batchSize: Int = DEFAULT_INDEX_BATCH_SIZE,
        convert: (CSVRecord) -> ElasticsearchDocument<T>
    ) {
        addFileConsumer { file ->
            esClient.withBatch(index, batchSize) { batch ->
                file.forEachCsvRecord { line ->
                    batch.addDocument(convert(line))
                }
            }
        }
    }

    fun FileImportTask.csvToRdf(
        batchSize: Int = DEFAULT_RDF_BATCH_SIZE,
        convert: (CSVRecord) -> Collection<Statement>
    ) {
        addFileConsumer { file ->
            rdfRepository.withStatementsBatch(batchSize) { rdfBatch ->
                file.forEachCsvRecord { csvRecord ->
                    rdfBatch.add(convert(csvRecord))
                }
            }
        }
    }

    fun FileImportTask.files(vararg fileNames: String, block: FileImportTask.() -> Unit) {
        val task = FileImportTask()
        block(task)
        task.getConsumers().forEach { consumer ->
            withFiles(consumer, *fileNames)
        }
    }

    private fun readLines(file: File, handleLine: (String) -> Unit) {
        var count = 0
        file.useLines { lines ->
            lines.forEachIndexed { index, line ->
                handleLine(line)
                count = index
                if (count > 0 && count % 1_000_000 == 0) {
                    log.info("${this::class.simpleName} processed $count lines from $file")
                }
            }
        }
        log.info("${this::class.simpleName} processed a total of $count lines from $file")
    }

    companion object {
        const val DEFAULT_RDF_BATCH_SIZE = 100_000
        const val DEFAULT_INDEX_BATCH_SIZE = 1000
        private val log = LoggerFactory.getLogger(DataImporter::class.java)
    }
}

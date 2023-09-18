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

    abstract fun createImportTask(): FileImportTask<*>

    fun <State> statefulTask(initialState: State, block: FileImportTask<State>.() -> Unit): FileImportTask<State> {
        val importTask = FileImportTask<State>()
        importTask.initState(initialState)
        block(importTask)
        return importTask
    }

    fun statelessTask(block: FileImportTask<Unit>.() -> Unit): StatelessTask {
        val importTask = StatelessTask()
        block(importTask)
        return importTask
    }

    private fun <State> FileImportTask<State>.runIfIndexMissing(index: String) {
        runIf { !esIndices.indexExists(index) }
    }

    private fun <State> FileImportTask<State>.createIndex(index: String) {
        onStart {
            esIndices.forceCreateIndex(index)
        }
    }

    fun <State> FileImportTask<State>.withIndex(index: String) {
        runIfIndexMissing(index)
        createIndex(index)
    }

    fun <T, State> FileImportTask<State>.index(
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

    fun <State> FileImportTask<State>.importRdf(
        statementBatchSize: Int = DEFAULT_RDF_BATCH_SIZE,
        convert: (String) -> Collection<Statement>
    ) {
        addFileConsumer { file ->
            rdfRepository.withStatementsBatch(statementBatchSize) { rdfBatch ->
                readLines(file) { rdfBatch.add(convert(it)) }
            }
        }
    }

    fun <T, State> FileImportTask<State>.indexCsv(
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

    fun <State> FileImportTask<State>.csvToRdf(
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

    fun <State> FileImportTask<State>.files(vararg fileNames: String, block: FileImportTask<State> .() -> Unit) {
        val task = FileImportTask<State>()
        block(task)
        task.getConsumers().forEach { consumer ->
            withFiles(consumer, *fileNames)
        }
    }

    private fun readLines(file: File, handleLine: (String) -> Unit) {
        var count = 1
        file.useLines { lines ->
            lines.forEach { line ->
                handleLine(line)
                count++
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

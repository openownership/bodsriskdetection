package org.bodsrisk.data.importer

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient
import io.slink.files.withTempDir
import org.bodsrisk.elasticsearch.DocumentBatch
import org.bodsrisk.elasticsearch.wipeIndex
import org.bodsrisk.elasticsearch.withBatch
import org.bodsrisk.utils.ThreadPool
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.StatementsBatch
import org.rdf4k.withStatementsBatch
import java.io.File

class ImportTask(
    private val rdfRepository: Repository,
    private val esClient: ElasticsearchClient,
    private val esIndices: ElasticsearchIndicesClient,
) {

    private val tasks = mutableListOf<(File) -> Unit>()
    private val indicesToWipe = mutableListOf<String>()
    lateinit var fileSource: FileSource

    fun source(source: FileSource): ImportTask {
        fileSource = source
        return this
    }

    fun importRdf(block: (File, StatementsBatch) -> Unit): ImportTask {
        tasks.add { file ->
            rdfRepository.withStatementsBatch(100_000) { rdfBatch ->
                block(file, rdfBatch)
            }
        }
        return this
    }

    fun <T> index(index: String, block: (File, DocumentBatch<T>) -> Unit): ImportTask {
        indicesToWipe.add(index)
        tasks.add { file ->
            esClient.withBatch(index) { batch ->
                block(file, batch)
            }
        }
        return this
    }

    fun run() {
        // This is unpleasant but we need a redesign of this to make it cleaner
        indicesToWipe.forEach { esIndices.wipeIndex(it) }

        withTempDir(File("temp")) { tempDir ->
            ThreadPool<Unit>(2).use { threadPool ->
                fileSource.loadFiles(tempDir).forEach { file ->
                    tasks.forEach { task -> threadPool.submit { task(file) } }
                }
            }
        }
    }
}
package org.bodsrisk.data.importer

import io.slink.files.TempDir
import io.slink.files.withTempDir
import org.bodsrisk.utils.ThreadPool
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Defines how a [DataImporter] will handle a [FileSource].
 *
 * @property source The FileSource to be used for this task
 * @property runIf Condition to validate this task should run (returns boolean)
 * @property onStart A function called before the task is run. This can be used for any initialisation logic
 * @property fileConsumers Handlers for the content of the files within the @source
 */
class FileImportTask<State> {

    lateinit var source: FileSource
    private var runIf: (() -> Boolean)? = null
    private var onStart = mutableListOf<(() -> Unit)>()
    private val fileConsumers = mutableListOf<(File) -> Unit>()
    private val onFinish = mutableListOf<(State) -> Unit>()
    private var internalState: State? = null

    val state: State get() = internalState!!

    val shouldRun: Boolean get() = runIf?.invoke() ?: true

    fun addFileConsumer(consumer: (File) -> Unit) {
        fileConsumers.add(consumer)
    }

    fun initState(state: State) {
        this.internalState = state
    }

    fun onFinish(block: (State) -> Unit) {
        onFinish.add(block)
    }

    fun withFiles(consumer: (File) -> Unit, vararg fileNames: String) {
        addFileConsumer { file ->
            if (file.name in fileNames) {
                consumer(file)
            }
        }
    }

    fun source(source: FileSource) {
        this.source = source
    }

    fun runIf(runIf: () -> Boolean) {
        this.runIf = runIf
    }

    fun onStart(block: () -> Unit) {
        this.onStart.add(block)
    }

    fun getConsumers(): List<(File) -> Unit> {
        return fileConsumers
    }


    private fun start() {
        onStart.forEach { it.invoke() }
    }

    private fun end() {
        if (internalState != null) {
            onFinish.forEach { it(internalState!!) }
        }
    }

    fun run(tempDir: TempDir, threadPoolSize: Int) {
        if (shouldRun) {
            start()
            val files = source.getFiles(tempDir)
            ThreadPool<Unit>(threadPoolSize).use { threadPool ->
                files.forEach { file ->
                    fileConsumers.forEach { consumer ->
                        threadPool.submit {
                            try {
                                consumer(file)
                            } catch (e: Exception) {
                                log.error("Error processing file $file", e)
                                throw e
                            }
                        }
                    }
                }
            }
            end()
        }
    }

    /**
     * For testing purposes only
     */
    internal fun runAllConsumers() {
        withTempDir(File("temp")) { tempDir ->
            source.getFiles(tempDir).forEach { file ->
                fileConsumers.forEach { consumer ->
                    consumer(file)
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileImportTask::class.java)
    }
}

typealias StatelessTask = FileImportTask<Unit>
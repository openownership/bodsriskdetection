package org.bodsrisk.data.importer

import io.slink.files.withTempDir
import java.io.File

/**
 * Defines how a [DataImporter] will handle a [FileSource].
 *
 * @property source The FileSource to be used for this task
 * @property runIf Condition to validate this task should run (returns boolean)
 * @property beforeStart A function called before the task is run. This can be used for any initialisation logic
 * @property fileConsumers Handlers for the content of the files within the @source
 */
class FileImportTask {

    lateinit var source: FileSource
    private var runIf: (() -> Boolean)? = null
    private var beforeStart = mutableListOf<(() -> Unit)>()
    private val fileConsumers = mutableListOf<(File) -> Unit>()

    fun addFileConsumer(consumer: (File) -> Unit) {
        fileConsumers.add(consumer)
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

    fun beforeStart(beforeStart: () -> Unit) {
        this.beforeStart.add(beforeStart)
    }

    fun getConsumers(): List<(File) -> Unit> {
        return fileConsumers
    }

    fun runnable(): Boolean {
        return runIf?.invoke() ?: true
    }

    fun runBeforeStart() {
        beforeStart.forEach { it.invoke() }
    }

    /**
     * For testing purposes only
     */
    fun runAllConsumers() {
        withTempDir(File("temp")) { tempDir ->
            source.forEachFile(tempDir) { file ->
                fileConsumers.forEach { consumer ->
                    consumer(file)
                }
            }
        }
    }
}
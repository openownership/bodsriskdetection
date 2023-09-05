package org.bodsrisk.data.importer

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly

class DataImporterTest : StringSpec({

    "files filter" {
        val importer = TestImporter()
        val task = importer.createImportTask()
        task.runAllConsumers()
        importer.file1Content shouldContainExactly listOf("a", "b", "c")
        importer.file2Content shouldContainExactly listOf("1", "2", "3")
        importer.allContent shouldContainExactly listOf("a", "b", "c", "1", "2", "3")
    }
})

class TestImporter : DataImporter() {

    val file1Content = mutableListOf<String>()
    val file2Content = mutableListOf<String>()
    val allContent = mutableListOf<String>()

    override fun createImportTask(): FileImportTask {
        return importTask {
            source(
                FileSource.Static(
                    "file1.txt" to """
                    a
                    b
                    c
                """.trimIndent(),
                    "file2.txt" to """
                    1
                    2
                    3
                """.trimIndent()
                )
            )
            files("file1.txt") {
                addFileConsumer { file1Content.addAll(it.readLines()) }
            }
            files("file2.txt") {
                addFileConsumer { file2Content.addAll(it.readLines()) }
            }
            addFileConsumer {
                allContent.addAll(it.readLines())
            }
        }
    }
}
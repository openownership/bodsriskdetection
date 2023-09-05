package org.bodsrisk.data.importer

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize

class FileImportTaskTest : StringSpec({

    "single job" {
        val content = mutableListOf<String>()
        val task = FileImportTask().apply {
            source(textSource)
            addFileConsumer {
                content.addAll(it.readLines())
            }
        }
        task.runAllConsumers()
        content shouldContainExactly sourceAsList
    }

    "multiple jobs" {
        val content = mutableListOf<String>()
        val task = FileImportTask().apply {
            source(textSource)
            addFileConsumer {
                content.addAll(it.readLines())
            }
            addFileConsumer {
                content.addAll(it.readLines())
            }
        }
        task.runAllConsumers()
        content shouldContainExactly sourceAsList.plus(sourceAsList)
    }

    "withFile - conditional" {
        val content = mutableListOf<String>()
        val task = FileImportTask().apply {
            source(textSource)
            withFiles({
                content.addAll(it.readLines())
            }, "file1.txt")
        }
        task.runAllConsumers()
        content shouldHaveSize 0
    }
})

private val content = """
        one
        two
        three
    """.trim()
private val textSource = FileSource.Static("file.txt" to content)

private val sourceAsList = content.split("\n").filter { it.isNotEmpty() }
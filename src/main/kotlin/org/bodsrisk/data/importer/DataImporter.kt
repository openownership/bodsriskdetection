package org.bodsrisk.data.importer

interface DataImporter {
    val requiresImport: Boolean
    fun buildTask(task: ImportTask)
}

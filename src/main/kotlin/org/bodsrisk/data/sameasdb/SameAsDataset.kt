package org.bodsrisk.data.sameasdb

import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.ImportTask
import org.bodsrisk.utils.toPojo

@Singleton
class SameAsDataset : DataImporter {

    override val requiresImport: Boolean = true

    override fun buildTask(task: ImportTask) {
        task.source(FileSource.Local("data/same-as-db.jsonl"))
            .importRdf { file, rdfBatch ->
                file.forEachLine {
                    val record = it.toPojo(SameAsRecord::class)
                    rdfBatch.add(record.rdfStatements())
                }
            }
    }
}
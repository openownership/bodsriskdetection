package org.bodsrisk.data.sameasdb

import jakarta.inject.Singleton
import org.bodsrisk.data.importer.DataImporter
import org.bodsrisk.data.importer.FileImportTask
import org.bodsrisk.data.importer.FileSource
import org.bodsrisk.data.importer.StatelessTask
import org.bodsrisk.utils.toPojo

@Singleton
class SameAsDataset : DataImporter() {

    override fun createImportTask(): StatelessTask {
        return statelessTask {
            source(FileSource.Local("data/same-as-db.jsonl"))
            importRdf { line ->
                line.toPojo(SameAsRecord::class)
                    .rdfStatements()
            }
        }
    }
}
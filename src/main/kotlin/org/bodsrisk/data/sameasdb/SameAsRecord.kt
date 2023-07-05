package org.bodsrisk.data.sameasdb

import org.bodsrisk.rdf.sameAs
import org.eclipse.rdf4j.model.Statement
import org.rdf4k.iri

data class SameAsRecord(
    val ids: MutableSet<String>,
    val source: String,
    val references: MutableList<String> = mutableListOf()
) {

    constructor(id: String, source: String) : this(ids = mutableSetOf(id), source = source)

    fun addId(id: String): SameAsRecord {
        ids.add(id)
        return this
    }

    fun addReference(text: String): SameAsRecord {
        references.add(text)
        return this
    }
}

// Leaving this as an extension function for now, because we might want to make namespaces (via BodsRisk) configurable
fun SameAsRecord.rdfStatements(): List<Statement> {
    val statements = mutableListOf<Statement>()
    val idsList = ids.toList()
    for (index in 0 until idsList.size - 1) {
        val id = idsList[index].iri()
        val nextId = idsList[index + 1].iri()
        statements.add(id.sameAs(nextId))
    }
    return statements
}

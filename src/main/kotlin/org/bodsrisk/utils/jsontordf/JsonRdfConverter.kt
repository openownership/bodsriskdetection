package org.bodsrisk.utils.jsontordf

import jakarta.json.JsonObject
import org.eclipse.rdf4j.model.Statement

interface JsonRdfConverter {
    fun convert(json: JsonObject): List<Statement>
}

class TypeConverter {
    val handlers = mutableMapOf<String, JsonRdfConverter>()
}
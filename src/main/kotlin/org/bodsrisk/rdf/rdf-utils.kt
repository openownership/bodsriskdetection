@file:Suppress("UNCHECKED_CAST")

package org.bodsrisk.rdf

import io.slink.resources.resourceAsString
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.repository.Repository
import org.kbods.rdf.BodsRdf
import org.rdf4k.iri
import org.rdf4k.sparqlSelect
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.statement

fun IRI.sameAs(iri: IRI): Statement {
    return statement(this, OWL.SAMEAS, iri)
}

fun Collection<IRI>.asStrings(): Collection<String> {
    return map { it.toString() }
}

fun IRI.statements(vararg po: Pair<IRI, Value>): List<Statement> {
    return po.map {
        statement(this, it.first, it.second)
    }
}

fun Repository.templateEntitiesQuery(classpathQuery: String, iris: Collection<IRI>): List<BindingSet> {
    val entitiesStr = iris.joinToString(" ") { "<$it>" }
    val query = resourceAsString(classpathQuery).replace("ENTITIES", entitiesStr)
    return sparqlSelect(query)
}

/**
 * Returns only the local names for the selected fields in this list of binding set
 */
fun Collection<BindingSet>.localNames(vararg fields: String): Set<String> {
    return allValues<IRI>(*fields).map { it.localName }.toSet()
}

fun <T : Value> Collection<BindingSet>.allValues(vararg fields: String): Set<T> {
    val values = mutableSetOf<Value>()
    forEach { binding ->
        val elements = fields.map { field ->
            binding.iri(field)
        }
        values.addAll(elements)
    }
    return values as Set<T>
}

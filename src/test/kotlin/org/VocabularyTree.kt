package org

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import java.io.File

class VocabularyTree(
    private val statements: Collection<Statement>,
    private val namespaces: Map<String, String>
) {

    private val subjects = mutableMapOf<String, MutableSet<IRI>>()

    init {
        statements
            .map { it.subject as IRI }
            .filter { it.namespace in namespaces && it.localName.isNotEmpty() }
            .forEach { subject ->
                subjects.putIfAbsent(subject.namespace, mutableSetOf())
                subjects[subject.namespace]!!.add(subject)
            }
    }

    fun write(directory: File, packageName: String) {
        subjects.forEach { (namespace, subjects) ->
            val prefix = namespaces[namespace]!!
            val subjectProps = subjects.map { subject ->
                val propName = subject.localName.replace(":", "_")
                """    val $propName: IRI = NAMESPACE.iri("${subject.localName}")"""
            }
            val code = """
package $packageName

import org.eclipse.rdf4j.model.IRI
import org.rdf4k.iri
import org.rdf4k.namespace

object ${prefix.uppercase()} {

    val NAMESPACE = "$namespace".namespace("$prefix")
    
${subjectProps.joinToString("\n")}
}
            """.trim()
            File(directory, "${prefix.uppercase()}.kt").writeText(code)
        }
    }

    companion object {
        private val defaultPrefixes = mapOf(
            "http://purl.org/dc/terms/" to "dcterms",
            "https://w3id.org/ftm#" to "ftm",
            "http://xmlns.com/foaf/0.1/" to "foaf",
            "http://purl.org/dc/elements/1.1/" to "dcelements",
//            "http://www.w3.org/2004/02/skos/core#" to "skos"
        )
    }
}
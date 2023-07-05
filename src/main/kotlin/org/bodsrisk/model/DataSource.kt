package org.bodsrisk.model

import org.bodsrisk.rdf.vocabulary.FTM
import org.eclipse.rdf4j.model.IRI
import org.kbods.rdf.BodsRdf

enum class DataSource(val iriPrefix: String) {
    OpenOwnership(BodsRdf.RESOURCE.name),
    OpenSanctions(FTM.NAMESPACE.name);

    fun matches(iri: String): Boolean {
        return iri.startsWith(iriPrefix)
    }

    companion object {
        fun forIri(iri: IRI): DataSource? {
            return forIri(iri.toString())
        }

        fun forIri(iri: String): DataSource? {
            return values().firstOrNull { it.matches(iri) }
        }
    }
}
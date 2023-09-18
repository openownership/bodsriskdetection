package org.bodsrisk.utils.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.eclipse.rdf4j.model.IRI
import org.rdf4k.iri
import org.rdf4k.toIri

class IriDeserializer : StdDeserializer<IRI?>(IRI::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): IRI? {
        val text = p.text
        return text?.toIri()
    }
}
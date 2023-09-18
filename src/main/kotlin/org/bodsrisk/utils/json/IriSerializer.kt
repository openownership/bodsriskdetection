package org.bodsrisk.utils.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.eclipse.rdf4j.model.IRI

class IriSerializer : StdSerializer<IRI>(IRI::class.java) {

    override fun serialize(value: IRI?, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value?.toString())
    }
}
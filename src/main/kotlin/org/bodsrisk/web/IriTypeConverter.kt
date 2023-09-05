package org.bodsrisk.web

import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.core.type.Argument
import io.micronaut.serde.Decoder
import io.micronaut.serde.Deserializer
import jakarta.inject.Singleton
import org.eclipse.rdf4j.model.IRI
import org.rdf4k.iri
import java.util.*

@Singleton
class IriTypeConverter : TypeConverter<String, IRI> {

    override fun convert(input: String?, targetType: Class<IRI>?, context: ConversionContext?): Optional<IRI> {
        return input?.let {
            Optional.of(input.iri())
        } ?: Optional.empty()
    }
}

@Singleton
class MicronautIriDeserializer : Deserializer<IRI> {
    override fun deserialize(decoder: Decoder, context: Deserializer.DecoderContext, type: Argument<in IRI>): IRI {
        return decoder.decodeString().iri()
    }
}


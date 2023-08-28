package org.bodsrisk.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.slink.http.text
import okhttp3.Response
import java.io.StringReader
import kotlin.reflect.KClass

object Json {
    val jacksonObjectMapper = jacksonObjectMapper()
    val klaxonJsonParser = Klaxon()

    init {
        jacksonObjectMapper.registerModule(JavaTimeModule())
        jacksonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}

fun Any.toJsonString(): String {
    return Json.jacksonObjectMapper.writeValueAsString(this)
}

fun <T : Any> String.toPojo(cls: KClass<T>): T {
    return Json.jacksonObjectMapper.readValue(this, cls.java)
}

fun String.toKlaxonJson(): JsonObject {
    return Json.klaxonJsonParser.parseJsonObject(StringReader(this))
}

fun Response.json(): JsonObject {
    return text().toKlaxonJson()
}

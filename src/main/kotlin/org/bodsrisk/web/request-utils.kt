package org.bodsrisk.web

import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.slink.string.urlEncode

//TODO: This is not recommended but it will do for this demo/PoC app
fun currentHttpRequest(): HttpRequest<Any> = ServerRequestContext.currentRequest<Any>().get()

fun HttpParameters.queryString(): String {
    val str = mutableListOf<String>()
    this.forEachValue { name, value ->
        str.add("$name=${value.urlEncode()}")
    }
    return str.joinToString("&")
}

fun HttpParameters.addParam(param: String, value: Any?): String {
    return addParams(mapOf(param to value))
}

fun HttpParameters.addParams(params: Map<String, Any?>): String {
    val str = mutableListOf<String>()

    // Keep params that aren't passed for replacements
    this.forEachValue { name, value ->
        if (name !in params.keys) {
            str.add("$name=${value.urlEncode()}")
        }
    }

    // Add the new params to be replaced (or added)
    params.forEach { (param, value) ->
        if (value != null) {
            str.add("$param=${value.toString().urlEncode()}")
        }
    }
    return str.joinToString("&")
}
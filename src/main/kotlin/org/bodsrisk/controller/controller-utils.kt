package org.bodsrisk.controller

import io.micronaut.http.HttpResponse
import java.net.URI

fun httpSeeOther(uri: String): HttpResponse<Any> {
    return HttpResponse.seeOther(URI(uri))
}

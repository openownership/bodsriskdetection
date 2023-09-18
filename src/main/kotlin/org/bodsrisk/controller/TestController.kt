package org.bodsrisk.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.View

@Controller
class TestController {

    @Get("/network-test")
    @View("network-test")
    fun networkTest(): HttpResponse<Any> {
        return HttpResponse.ok()
    }
}

package org.bodsrisk.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller
class RootController {

    @Get("/")
    fun root() = httpSeeOther("/search")
}
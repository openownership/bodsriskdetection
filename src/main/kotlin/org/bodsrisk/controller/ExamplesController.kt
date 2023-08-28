package org.bodsrisk.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.View
import io.slink.resources.resourceAsInput

@Controller
class ExamplesController {

    private val mapper = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())
    private val examples = mapper.readValue(resourceAsInput("examples.yml"), Examples::class.java)

    @Get("/examples")
    @View("examples")
    fun examples(): Examples = examples
}

@Introspected
data class Examples(
    val categories: List<Category>
) {
    data class Category(
        val name: String,
        val description: String,
        val links: List<String>
    )
}

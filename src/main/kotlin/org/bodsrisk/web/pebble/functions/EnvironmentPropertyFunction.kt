package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import io.micronaut.context.env.Environment

class EnvironmentPropertyFunction(private val environment: Environment) : FunctionBase {

    override val name: String = "envProperty"
    override fun getArgumentNames() = listOf(ARG)

    override fun execute(
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        val property = args[ARG]!! as String
        return environment.getProperty(property, String::class.java).get()
    }

    companion object {
        private const val ARG = "property"
    }
}

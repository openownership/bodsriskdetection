package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import io.slink.iso3166.Country

class CountryFunction : FunctionBase {

    override val name: String = "country"
    override fun getArgumentNames() = listOf(ARG)

    override fun execute(
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any? {
        val arg = args[ARG] ?: return null
        return Country.byCode(arg as String)
    }

    companion object {
        private const val ARG = "alpha2Or3Code"
    }
}

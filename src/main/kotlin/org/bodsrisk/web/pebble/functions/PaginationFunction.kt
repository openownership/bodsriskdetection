package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import io.micronaut.data.model.Page
import org.bodsrisk.data.page.Pagination

class PaginationFunction : FunctionBase {

    override val name: String = "pagination"
    override fun getArgumentNames() = listOf(ARG)

    override fun execute(
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        val page = args[ARG]!! as Page<*>
        return Pagination(page)
    }

    companion object {
        private const val ARG = "page"
    }
}

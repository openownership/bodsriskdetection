package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import io.slink.id.uuid

class UuidFunction : NoArgsFunction {

    override val name: String = "uuid"

    override fun execute(
        args: MutableMap<String, Any>?,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        return uuid()
    }
}
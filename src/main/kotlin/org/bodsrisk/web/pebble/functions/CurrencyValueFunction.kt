package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import io.slink.currency.fractionalAmountToString
import java.util.*


class CurrencyValueFunction : FunctionBase {

    override val name: String = "currencyValue"

    override fun getArgumentNames(): List<String> = listOf(ARG_VALUE, ARG_CURRENCY)

    override fun execute(
        args: MutableMap<String, Any>,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any {
        val decimalAmount = ((args[ARG_VALUE]!! as Double) * 100).toLong()
        return Currency.getInstance(args[ARG_CURRENCY]!! as String)
            .fractionalAmountToString(decimalAmount)
    }

    companion object {
        private const val ARG_VALUE = "value"
        private const val ARG_CURRENCY = "currency"
    }
}
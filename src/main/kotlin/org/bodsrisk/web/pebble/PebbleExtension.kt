package org.bodsrisk.web.pebble

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Function
import io.micronaut.context.env.Environment
import jakarta.inject.Singleton
import org.bodsrisk.web.pebble.functions.*
import org.bodsrisk.web.pebble.globalvars.RequestGlobalVar

@Singleton
class PebbleExtension(private val environment: Environment) : AbstractExtension() {

    override fun getFunctions(): Map<String, Function> {
        return listOf(
            EnvironmentPropertyFunction(environment),
            UuidFunction(),
            PaginationFunction(),
            CurrencyValueFunction(),
            CountryFunction()
        ).associateBy { it.name }
    }

    override fun getGlobalVariables(): Map<String, Any> {
        return listOf(
            RequestGlobalVar()
        ).associateBy { it.name }
    }
}

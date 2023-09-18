package org.bodsrisk.web.pebble.functions

interface NoArgsFunction : FunctionBase {
    override fun getArgumentNames() = emptyList<String>()
}
package org.bodsrisk.web.pebble.functions

import com.mitchellbosecke.pebble.extension.Function

interface FunctionBase : Function {
    val name: String
}
package org.bodsrisk.web.pebble.globalvars

import org.bodsrisk.web.addParam
import org.bodsrisk.web.addParams
import org.bodsrisk.web.currentHttpRequest

class RequestGlobalVar : GlobalVariable {

    override val name: String = "request"

    fun attr(attr: String): Any {
        return currentHttpRequest().getAttribute(attr, Any::class.java).get()
    }

    fun addParam(name: String, value: Any?): String {
        val request = currentHttpRequest()
        return request.path + "?" + currentHttpRequest().parameters.addParam(name, value)
    }

    fun addParams(params: Map<String, Any?>): String {
        val request = currentHttpRequest()
        return request.path + "?" + request.parameters.addParams(params)
    }
}
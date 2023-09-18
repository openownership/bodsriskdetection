package org.bodsrisk.utils.i18n

import io.micronaut.context.LocalizedMessageSource
import jakarta.inject.Singleton
import kotlin.jvm.optionals.getOrDefault

@Singleton
class I18n(private val messagesBean: LocalizedMessageSource) {

    init {
        messages = messagesBean
    }

    companion object {
        private lateinit var messages: LocalizedMessageSource

        fun get(key: String): String {
            return messages.getMessage(key).getOrDefault(key)
        }
    }
}
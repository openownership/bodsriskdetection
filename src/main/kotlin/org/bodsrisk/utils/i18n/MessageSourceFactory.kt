package org.bodsrisk.utils.i18n

import io.micronaut.context.MessageSource
import io.micronaut.context.annotation.Factory
import io.micronaut.context.i18n.ResourceBundleMessageSource
import jakarta.inject.Singleton

@Factory
class MessageSourceFactory {

    @Singleton
    fun createMessageSource(): MessageSource = ResourceBundleMessageSource("i18n.messages")
}

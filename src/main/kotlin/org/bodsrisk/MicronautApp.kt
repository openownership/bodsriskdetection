package org.bodsrisk

import io.micronaut.runtime.Micronaut

fun main(args: Array<String>) {
    Micronaut.build().args(*args)
        .packages("org.bodsrisk")
        .eagerInitSingletons(true)
        .start()
}

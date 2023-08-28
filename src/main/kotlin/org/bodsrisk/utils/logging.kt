package org.bodsrisk.utils

import org.slf4j.Logger
import java.time.Duration


fun <T> timed(function: () -> T): Pair<Duration, T> {
    val startTime = System.nanoTime()
    val result = function()
    val duration = System.nanoTime() - startTime
    return Pair(Duration.ofNanos(duration), result)
}

fun <T> Logger.timedInfo(name: String, function: () -> T): T {
    val response = timed(function)
    if (isInfoEnabled) {
        info("{} took {}", name, response.first)
    }
    return response.second
}

fun <T> Logger.timedDebug(name: String, function: () -> T): T {
    val response = timed(function)
    if (isDebugEnabled) {
        debug("{} took {}", name, response.first)
    }
    return response.second
}

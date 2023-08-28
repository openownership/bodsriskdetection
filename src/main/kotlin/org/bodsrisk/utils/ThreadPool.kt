package org.bodsrisk.utils

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ThreadPool<T>(
    threadCount: Int,
    private val timeout: Long = 1,
    private val timeoutUnit: TimeUnit = TimeUnit.MINUTES
) : AutoCloseable {

    private val threadPool = Executors.newFixedThreadPool(threadCount)
    private val futures = mutableListOf<Future<T>>()

    fun submit(task: () -> T): Future<T> {
        val future = threadPool.submit(task)
        futures.add(future)
        return future
    }

    override fun close() {
        futures.forEach { it.get() }
        threadPool.shutdown()
        threadPool.awaitTermination(timeout, timeoutUnit)
    }
}
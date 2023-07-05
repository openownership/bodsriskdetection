package org.bodsrisk.data.page

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable

data class ResultsPage<T>(
    val fieldContent: List<T>,
    val fieldPageable: Pageable,
    val fieldTotalSize: Long
) : Page<T> {

    override fun getContent(): List<T> = fieldContent
    override fun getPageable(): Pageable = fieldPageable
    override fun getTotalSize(): Long = fieldTotalSize
}
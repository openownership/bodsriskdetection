package org.bodsrisk.data.page

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable


class PaginationTest : StringSpec({

    "basic paging" {
        Pagination(PageImpl(10, 0)).assertIs(listOf(0))
            .assertCurrentPage(0)
            .assertHasPrev(false)
            .assertHasNext(false)
            .assertPrev(null)
            .assertNext(null)

        Pagination(PageImpl(20, 0)).assertIs(listOf(0, 1))
            .assertCurrentPage(0)
            .assertHasPrev(false)
            .assertHasNext(true)
            .assertPrev(null)
            .assertNext(1)

        Pagination(PageImpl(20, 1)).assertIs(listOf(0, 1))
            .assertCurrentPage(1)
            .assertHasPrev(true)
            .assertHasNext(false)

        Pagination(PageImpl(20, 0, 5)).assertIs(listOf(0, 1, 2, 3))
            .assertCurrentPage(0)
            .assertHasPrev(false)
            .assertHasNext(true)

        Pagination(PageImpl(20, 1, 5)).assertIs(listOf(0, 1, 2, 3))
            .assertCurrentPage(1)
            .assertHasPrev(true)
            .assertHasNext(true)

        Pagination(PageImpl(23, 2)).assertIs(listOf(0, 1, 2))
            .assertCurrentPage(2)
            .assertHasPrev(true)
            .assertHasNext(false)

        Pagination(PageImpl(100, 1)).assertIs((0..9).toList())
        Pagination(PageImpl(90, 1)).assertIs((0..8).toList())
        Pagination(PageImpl(70, 1)).assertIs((0..6).toList())

        Pagination(PageImpl(70, 6)).assertIs((0..6).toList())
            .assertCurrentPage(6)
            .assertHasPrev(true)
            .assertHasNext(false)
            .assertPrev(5)
            .assertNext(null)
    }

    "complex paging"() {
        Pagination(PageImpl(1000, 1)).assertIs(listOf(0, 1, 2, 3, 4, null, 99))
            .assertCurrentPage(1)
            .assertHasPrev(true)
            .assertHasNext(true)

        Pagination(PageImpl(1000, 2)).assertIs(listOf(0, 1, 2, 3, 4, null, 99))
            .assertCurrentPage(2)
            .assertHasPrev(true)
            .assertHasNext(true)

        Pagination(PageImpl(1000, 4)).assertIs(listOf(0, null, 3, 4, 5, null, 99))
        Pagination(PageImpl(1000, 72)).assertIs(listOf(0, null, 71, 72, 73, null, 99))
        Pagination(PageImpl(995, 72)).assertIs(listOf(0, null, 71, 72, 73, null, 99))

        // Last pages
        Pagination(PageImpl(1000, 94)).assertIs(listOf(0, null, 93, 94, 95, null, 99))
        Pagination(PageImpl(1000, 95)).assertIs(listOf(0, null, 94, 95, 96, null, 99))
        Pagination(PageImpl(1000, 96)).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(PageImpl(1000, 97)).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(PageImpl(1000, 98)).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
        Pagination(PageImpl(1000, 99)).assertIs(listOf(0, null, 95, 96, 97, 98, 99))
    }
})

class PageImpl(
    val totalResultSize: Long,
    val currenPageNumber: Int,
    val pageSize: Int = 10
) : Page<Any> {
    override fun getTotalSize(): Long = totalResultSize
    override fun getContent(): List<Any> = emptyList()

    override fun getPageable(): Pageable = PageableImpl(pageSize, currenPageNumber)
}

class PageableImpl(
    val pageSize: Int,
    val currentPageNumber: Int
) : Pageable {
    override fun getNumber(): Int = currentPageNumber
    override fun getSize(): Int = pageSize
}

fun Pagination.assertIs(list: List<Int?>): Pagination {
    this.pageNumbers shouldContainExactly list
    return this
}

fun Pagination.assertCurrentPage(currentPageNumber: Int): Pagination {
    this.currentPage shouldBe currentPageNumber
    return this
}

fun Pagination.assertHasPrev(value: Boolean): Pagination {
    this.hasPrev shouldBe value
    return this
}

fun Pagination.assertHasNext(value: Boolean): Pagination {
    this.hasNext shouldBe value
    return this
}

fun Pagination.assertPrev(page: Int?): Pagination {
    this.prevPage shouldBe page
    return this
}

fun Pagination.assertNext(page: Int?): Pagination {
    this.nextPage shouldBe page
    return this
}
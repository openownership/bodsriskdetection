package org.bodsrisk.data.page

import io.micronaut.data.model.Page

/**
 * This is an (opinionated) implementation of a helper component for displaying pagination controls in web applications.
 * For long sets of pages, the algorithm is designed to compress the display in order to maintain a reasonable
 * size for the pagination listing. For example, with a page 12 selected in a set of 100 pages, we'd have something like this: [1 ... 11 <12> 13 ... 100]
 *
 * @pageNumbers returns the list of page numbers that should be displayed, using null values for where ellipsis (...) or some other "intermediate pages" symbol
 * should be presented. Please note that these numbers are zero-indexed [0 1 2 ...] so for display purposes these should be converted to 1-indexed values [1 2 3 ...].
 *
 * When the total number of pages is <=10 the algorithm will return all pages: [1 2 3 4 5 6 7 8 9 10]
 *
 * When the total number of pages is >10 the following logic applies:
 * (1): We always display a maximum of 7 page elements, including ellipsis (null) indicators: [1 null 12 <13> 14 null 100]
 * (2): The current page should always have the previous and next pages displayed. For example if the current page is 12
 * then we display [1 null 11 <12> 13 null 100] and not [1 null <12> 13 14 null 100]
 * (3): When the current page is in the first 4 pages we display all first 5 pages: [1 2 3 <4> 5 null 100]. This is also in line with rule (2).
 * (4): When the current page is in the last 4 pages we display all last 5 pages: [1 null 96 <97> 98 99 100]. This is also in line with rule (2).
 * (5): First and last pages should always be reachable: the pagination should always display the controls for the first and last page.
 */
class Pagination(val page: Page<*>) {

    val pageNumbers: List<Int?> = createPages()
    val hasPrev: Boolean = page.pageNumber > 0
    val hasNext: Boolean = page.pageNumber < page.totalPages - 1
    val currentPage: Int = page.pageNumber
    val prevPage: Int? = if (hasPrev) currentPage - 1 else null
    val nextPage: Int? = if (hasNext) currentPage + 1 else null

    private fun createPages(): List<Int?> {

        val totalPages = page.totalPages
        val currentPage = page.pageNumber

        // If at most 10 pages then display all of them
        if (totalPages <= 10) {
            return (0 until totalPages).toList()
        }

        // Otherwise display "..." (null) for intermediate pages
        return when {
            // Selected page is from 1 to 4
            currentPage <= 3 -> {
                listOf(0, 1, 2, 3, 4, null, totalPages - 1)
            }

            // Selected page is in the last 4 pages
            currentPage >= totalPages - 4 -> {
                listOf(0, null, totalPages - 5, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1)
            }

            // Selected page is in the middle somewhere, display first, last and adjacent pages
            else -> {
                return listOf(0, null, currentPage - 1, currentPage, currentPage + 1, null, totalPages - 1)
            }
        }
    }
}

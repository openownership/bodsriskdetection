package org.bodsrisk.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.*
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.util.ObjectBuilder
import io.micronaut.data.model.Pageable
import org.bodsrisk.data.page.ResultsPage

fun SearchRequest.Builder.page(pageable: Pageable): SearchRequest.Builder {
    return from(pageable.number * pageable.size)
        .size(pageable.size)
}

fun SearchRequest.Builder.match(field: String, searchTerm: String): SearchRequest.Builder {
    return query { q ->
        q.match(field, searchTerm)
    }
}

fun Query.Builder.match(field: String, searchTerm: String): ObjectBuilder<Query> {
    return match {
        it.field(field)
            .query(searchTerm)
            .operator(Operator.And)
    }
}

fun <T, R> SearchResponse<T>.toPage(pageable: Pageable, map: (T) -> R): ResultsPage<R> {
    return ResultsPage(
        fieldContent = hits().hits().map { map(it.source()!!) },
        fieldPageable = pageable,
        fieldTotalSize = hits().total()!!.value()
    )
}

fun String.termQuery(value: String): TermQuery {
    return QueryBuilders.term()
        .field(this)
        .value(value)
        .build()
}

fun Query.Builder.term(field: String, value: String): ObjectBuilder<Query> {
    return term(field.termQuery(value))
}

fun Query.Builder.terms(field: String, values: Collection<String>): ObjectBuilder<Query> {
    return terms(field.termsQuery(values))
}

fun Query.Builder.terms(field: String, vararg values: String): ObjectBuilder<Query> {
    return terms(field.termsQuery(values.toList()))
}

fun String.termsQuery(values: Collection<String>): TermsQuery {
    return QueryBuilders.terms()
        .field(this)
        .terms(TermsQueryField.Builder().value(values.map { FieldValue.of(it) }).build())
        .build()
}

fun String.termsQuery(vararg values: String): TermsQuery {
    return termsQuery(values.toList())
}
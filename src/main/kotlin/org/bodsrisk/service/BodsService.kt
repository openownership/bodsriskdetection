package org.bodsrisk.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import org.bodsrisk.data.openownership.OpenOwnershipDataset
import org.bodsrisk.elasticsearch.*
import org.bodsrisk.model.Entity
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.bodsType
import org.bodsrisk.model.toEntity
import org.bodsrisk.rdf.localNames
import org.eclipse.rdf4j.query.BindingSet
import org.kbods.read.BodsStatement
import org.kbods.read.BodsStatementType

@Singleton
class BodsService(
    private val esClient: ElasticsearchClient
) {

    fun search(searchTerm: String, entityType: EntityType?, pageable: Pageable): Page<Entity> {
        return esClient.search({ request ->
            request
                .index(OpenOwnershipDataset.INDEX)
                .page(pageable)
                .query { q ->
                    if (searchTerm.startsWith(PREFIX_STATEMENT_ID)) {
                        // Looking for a specific statement
                        q.term(FIELD_STATEMENT_ID, searchTerm)
                    } else {
                        q.byName(searchTerm, entityType?.bodsType)
                    }
                }
                .sort { sort ->
                    sort.score { it.order(SortOrder.Desc) }
                }
        }, JsonData::class.java)
            .toPage(pageable) {
                BodsStatement(it.toJson().toString()).toEntity()
            }
    }

    private fun Query.Builder.byName(searchTerm: String, statementType: BodsStatementType?): ObjectBuilder<Query> {
        return bool { bool ->
            bool.must { it.match(FIELD_ALL_NAMES, searchTerm) }
            if (statementType != null) {
                bool.must { q ->
                    q.term(FIELD_STATEMENT_TYPE, statementType.type)
                }
            } else {
                // Make sure only entities and people are searched
                bool.must { q ->
                    q.terms(FIELD_STATEMENT_TYPE, BodsStatementType.ENTITY.type, BodsStatementType.PERSON.type)
                }
            }
            bool
        }
    }

    fun getStatements(statementIds: Collection<String>): List<BodsStatement> {
        return esClient.findByTerms<JsonData>(OpenOwnershipDataset.INDEX, FIELD_STATEMENT_ID, statementIds)
            .map { BodsStatement(it.source()!!.toJson().toString()) }
    }

    fun getStatements(sparqlBindings: Collection<BindingSet>, vararg fields: String): Map<String, BodsStatement> {
        return getStatements(sparqlBindings.localNames(*fields))
            .associateBy { it.id }
    }

    companion object {
        private const val FIELD_ALL_NAMES = "allNames"
        private const val FIELD_STATEMENT_ID = "statementID"
        private const val FIELD_STATEMENT_TYPE = "statementType"
        private const val PREFIX_STATEMENT_ID = "openownership-register"
    }
}

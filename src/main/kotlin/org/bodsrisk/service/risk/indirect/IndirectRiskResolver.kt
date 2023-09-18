package org.bodsrisk.service.risk.indirect

import jakarta.inject.Inject
import org.bodsrisk.model.EntityType
import org.bodsrisk.model.RelatedEntity
import org.bodsrisk.model.RelatedEntityRelationship
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.model.risk.Risks
import org.bodsrisk.model.risk.sort
import org.bodsrisk.rdf.allValues
import org.bodsrisk.service.entityresolver.EntityResolver
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.str

abstract class IndirectRiskResolver {

    @Inject
    lateinit var rdfRepository: Repository

    @Inject
    lateinit var entityResolver: EntityResolver

    abstract val entityTypes: Set<EntityType>
    abstract val relationshipType: RelatedEntityRelationship
    abstract val findQueryPath: String
    abstract val order: Int

    fun find(target: IRI): List<RelatedEntity<Risks>> {
        val results = rdfRepository.sparqlSelectClasspath(findQueryPath, "target" to target)
        val entitiesByIri = entityResolver.resolveEntities(results.allValues("riskEntity"))

        return results.associateBy { it.iri("riskEntity") }
            .filter { entitiesByIri[it.key] != null }
            .map { entry ->
                val iri = entry.key
                val row = entry.value
                val entity = entitiesByIri[iri]!!
                val intermediateEntity = if (row.bindingNames.contains("intermediateEntity")) {
                    row.iri("intermediateEntity")
                } else {
                    null
                }

                RelatedEntity(
                    target = target,
                    entity = entity,
                    relationship = relationshipType,
                    intermediateEntity = intermediateEntity,
                    data = row.str("risks").split(",")
                        .map { risk -> Risk(risk) }
                        .sort()
                )
            }
    }
}

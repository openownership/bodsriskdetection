package org.bodsrisk.service.risk

import jakarta.inject.Singleton
import org.bodsrisk.data.opensanctions.OpenSanctionsDataset
import org.bodsrisk.model.Entity
import org.bodsrisk.model.RelatedEntity
import org.bodsrisk.model.risk.Risk
import org.bodsrisk.model.risk.RiskProfile
import org.bodsrisk.model.risk.Risks
import org.bodsrisk.rdf.templateEntitiesQuery
import org.bodsrisk.service.risk.indirect.IndirectRiskResolver
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.rdf4k.iri
import org.rdf4k.sparqlSelectClasspath
import org.rdf4k.str

@Singleton
class RiskService(
    private val rdfRepository: Repository,
    private val openSanctionsDataset: OpenSanctionsDataset,
    relatedRiskResolvers: List<IndirectRiskResolver>
) {

    private val riskResolvers = relatedRiskResolvers.sortedBy { it.order }

    fun risksForEntities(iris: Collection<IRI>): Map<IRI, Risks> {
        return rdfRepository.templateEntitiesQuery("sparql/risk/risks-for-entities.template.sparql", iris)
            .associate {
                val risks = it.str("risks").split(",").toSet().map { ftmTopic -> Risk(ftmTopic) }
                it.iri("entity") to risks
            }
    }

    fun risksForEntity(iri: IRI): Risks {
        return risksForEntities(listOf(iri)).values.firstOrNull() ?: emptyList()
    }

    fun getRiskProfile(target: IRI): RiskProfile? {
        val recordId = rdfRepository.sparqlSelectClasspath("sparql/open-sanctions-record.sparql", "target" to target)
            .firstOrNull()
            ?.iri("openSanctionsRecord")
            ?.localName

        return recordId?.let {
            openSanctionsDataset.getRecord(it)
        }?.let {
            RiskProfile.fromJson(it.toJson().toString())
        }
    }

    fun relatedEntitiesRisks(target: Entity): List<RelatedEntity<Risks>> {
        val relatedEntityRisks = mutableListOf<RelatedEntity<Risks>>()
        val ids = mutableSetOf<IRI>()
        riskResolvers
            .filter { target.type in it.entityTypes }
            .forEach { riskResolver ->
                val relatedEntities = riskResolver.find(target.iri)
                val newEntities = relatedEntities.filter { it.entity.iri !in ids }
                relatedEntityRisks.addAll(newEntities)
                ids.addAll(newEntities.map { it.entity.iri })
            }
        return relatedEntityRisks
            .toSet()
            .sortedBy { it.entity.name }
    }
}

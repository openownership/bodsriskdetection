package org.bodsrisk.data.publiccontracts

import com.beust.klaxon.JsonObject
import com.github.michaelbull.retry.ContinueRetrying
import com.github.michaelbull.retry.StopRetrying
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import io.slink.http.SlinkHttpException
import io.slink.http.checkOk
import io.slink.http.get
import io.slink.http.newHttpClient
import kotlinx.coroutines.runBlocking
import org.bodsrisk.utils.json
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate

fun main(args: Array<String>) {
    ContractsFinderApi.download(File("data/gb-public-contracts"))
}

object ContractsFinderApi {

    private val httpClient = newHttpClient()

    fun download(directory: File) {
        val currentYear = LocalDate.now().year
        log.info("Loading UK Public Contracts for period 2016..$currentYear")
        for (year in 2016..currentYear) {
            val file = File(directory, "uk-public-contracts-$year.jsonl")
            file.bufferedWriter().use { writer ->
                var url: String? = yearUrl(year)
                while (url != null) {
                    url = loadContracts(url) {
                        writer.write(it.toJsonString())
                        writer.write("\n")
                    }
                }
            }
        }
    }

    private fun loadContracts(url: String, handleContract: (JsonObject) -> Unit): String? {
        return runBlocking {
            retry(retryPolicy) {
                val jsonResponse = httpClient.get(url)
                    .checkOk()
                    .json()

//                val results = jsonResponse.array<JsonObject>("releases")?.filter { it.hasAward }
                val results = jsonResponse.array<JsonObject>("releases")
                results?.forEach { contract ->
                    handleContract(contract)
                }

                log.info("Read ${results?.size ?: 0} contracts from $url")
                jsonResponse.obj("links")?.string("next")
            }
        }
    }

    private fun yearUrl(year: Int): String {
        return "https://www.contractsfinder.service.gov.uk/Published/Notices/OCDS/Search?publishedFrom=${year}-01-01T00:00:00&publishedTo=${year}-12-31T23:59:59&stages=award,implementation"
    }

    private val log = LoggerFactory.getLogger(ContractsFinderApi::class.java)

    // Deeply unpleasant, but the reason is here (bottom of the page):
    // https://www.contractsfinder.service.gov.uk/apidocumentation/Notices/1/GET-Published-Notice-OCDS-Search
    private val retryOn403: RetryPolicy<Throwable> = {
        if (reason is SlinkHttpException && (reason as SlinkHttpException).code == 403) {
            log.warn("Received 403 and body ${(reason as SlinkHttpException).responseBody}. Sleeping and retrying")
            ContinueRetrying
        } else {
            StopRetrying
        }
    }
    private val retryPolicy = retryOn403 + limitAttempts(2) + constantDelay(310000)
}
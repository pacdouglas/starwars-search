package io.github.pacdouglas.starwarssearch.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.pacdouglas.starwarssearch.model.SwApiRawData
import io.github.pacdouglas.starwarssearch.repository.SwApiRawDataRepository
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

class PopulateDatabase(private val rest: RestTemplate,
                       private val repo: SwApiRawDataRepository) : Runnable {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val mapper = jacksonObjectMapper()

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Title(val title: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Name(val name: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class PaginatedResult(val count: Int, val next: String? = null, val results: List<Map<String, Any?>>)

    companion object {
        private const val BASE_API_URL = "https://swapi.dev/api"
    }

    override fun run() {
        logger.info("importing raw data from 'Star Wars API'")
        arrayOf("films", "people", "planets", "species", "starships", "vehicles").forEach(::handleRequest)
        logger.info("import raw data from 'Star Wars API' done")
    }

    private fun handleRequest(type: String) {
        runUntilNullResult(1 to "$BASE_API_URL/$type") { (partial, url) ->
            val result = fetch(url)

            result.results.map(mapper::writeValueAsString).map { json ->
                SwApiRawData(generateSearchableName(json), type, json)
            }.forEachIndexed { i, rawData ->
                logger.info("insert [${i + partial}/${result.count}]$rawData")
                repo.save(rawData)
            }

            result.next?.let { nextUrl ->
                (result.results.count() + partial) to nextUrl
            }
        }
    }

    private fun fetch(url: String): PaginatedResult {
        return rest.getForObject(url)
    }

    private fun generateSearchableName(json: String): String {
        return runCatching { mapper.readValue<Name>(json).name }.getOrNull()
                ?: runCatching { mapper.readValue<Title>(json).title }.getOrNull()
                ?: error("Error trying find the searchable name to $json")
    }

    private inline fun <T> runUntilNullResult(initial: T, block: (acc: T) -> T?) {
        var last: T? = null
        do {
            last = block(last ?: initial)
        } while (last != null)
    }
}
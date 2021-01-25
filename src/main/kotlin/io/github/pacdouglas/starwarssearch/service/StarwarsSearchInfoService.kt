package io.github.pacdouglas.starwarssearch.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.CacheBuilder
import info.debatty.java.stringsimilarity.JaroWinkler
import info.debatty.java.stringsimilarity.LongestCommonSubsequence
import info.debatty.java.stringsimilarity.MetricLCS
import io.github.pacdouglas.starwarssearch.model.StarWarsInfoSearchCount
import io.github.pacdouglas.starwarssearch.model.StarwarsInfo
import io.github.pacdouglas.starwarssearch.model.SwApiRawData
import io.github.pacdouglas.starwarssearch.repository.StarWarsInfoSearchCountRepository
import io.github.pacdouglas.starwarssearch.repository.SwApiRawDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@Service
class StarwarsSearchInfoService(private val rawDataRepo: SwApiRawDataRepository,
                                private val searchCountRepo: StarWarsInfoSearchCountRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val mapper = jacksonObjectMapper()

    private val allData by lazy {
        rawDataRepo.findAll().also(::initializeAllCache)
    }

    private val cacheSimilar = CacheBuilder.newBuilder()
            .softValues()
            .build<SwApiRawData, Set<SwApiRawData>>()

    fun find(type: String, partOfName: String): StarwarsInfo {
        val filteredByType = allData.filter { it.type == type }

        val found = findAsLike(filteredByType, partOfName)
                ?: findAsJaroWinkler(filteredByType, partOfName)

        updateCountTable(partOfName, found)

        if (found == null) {
            return StarwarsInfo(emptyMap(), emptyList())
        }

        val similar = buildSimilarInfo(filteredByType, found).shuffled().take(3)

        return StarwarsInfo(mapper.readValue(found.data), similar.map { mapper.readValue(it.data) })
    }

    private fun findAsLike(filteredByType: List<SwApiRawData>, partOfName: String): SwApiRawData? {
        val normalizedPartOfName = partOfName.onlyNumbersAndLetters()

        return filteredByType.firstOrNull {
            val normalizedName = it.name.onlyNumbersAndLetters()
            normalizedName == normalizedPartOfName || normalizedName.contains(normalizedPartOfName)
        }
    }

    private fun findAsJaroWinkler(filteredByType: List<SwApiRawData>, partOfName: String): SwApiRawData? {
        val normalizedPartOfName = partOfName.onlyNumbersAndLetters()
        val jaroWinkler = JaroWinkler()

        return filteredByType.firstOrNull {
            jaroWinkler.distance(normalizedPartOfName, it.name.onlyNumbersAndLetters()) < 0.2
        }
    }

    private fun String.onlyNumbersAndLetters(): String {
        return this.toUpperCase().replace("[^A-Z0-9]+".toRegex(), "")
    }

    private fun buildSimilarInfo(filteredByType: List<SwApiRawData>, found: SwApiRawData): Set<SwApiRawData> {
        val fromCache = cacheSimilar.getIfPresent(found)

        if (fromCache != null) {
            return fromCache
        }

        val subtractThis = filteredByType.subtract(setOf(found))

        val byLongestCommon = subtractThis.sortedBy {
            LongestCommonSubsequence().distance(found.data, it.data)
        }.take(4)

        val byJaroWinkler = subtractThis.sortedBy {
            JaroWinkler().distance(found.data, it.data)
        }.take(4)

        val byMetricLCS = subtractThis.sortedBy {
            MetricLCS().distance(found.data, it.data)
        }.take(4)

        return (byLongestCommon + byJaroWinkler + byMetricLCS).toSet().also {
            logger.info("Caching similarity from [{}][{}] to {}", found.type, found.name, it.joinToString(transform = SwApiRawData::name))
            synchronized(this) {
                cacheSimilar.put(found, it)
            }
        }
    }

    private fun initializeAllCache(data: Iterable<SwApiRawData>) {
        thread {
            data.forEach { current ->
                val filteredByType = allData.filter { it.type == current.type }
                buildSimilarInfo(filteredByType, current)
            }
        }
    }

    private val threadPool = Executors.newFixedThreadPool(1)
    private fun updateCountTable(partOfName: String, found: SwApiRawData?) {
        threadPool.submit {
            val searchCountEntity = searchCountRepo.findById(partOfName).orElseGet {
                StarWarsInfoSearchCount(partOfName, found)
            }
            searchCountRepo.save(searchCountEntity.copy(count = searchCountEntity.count + 1L))
        }
    }
}
package io.github.pacdouglas.starwarssearch.data

import io.github.pacdouglas.starwarssearch.model.StarWarsInfoSearchCount
import io.github.pacdouglas.starwarssearch.model.SwApiRawData
import io.github.pacdouglas.starwarssearch.repository.StarWarsInfoSearchCountRepository
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

object MetricsUpdater {
    private val threadPool = Executors.newFixedThreadPool(1) as ThreadPoolExecutor

    fun updateCountTable(searchCountRepo: StarWarsInfoSearchCountRepository, searchedTerm: String, found: SwApiRawData?) {
        threadPool.submit {
            val searchCountEntity = searchCountRepo.findById(searchedTerm).orElseGet {
                StarWarsInfoSearchCount(searchedTerm, found)
            }
            searchCountRepo.save(searchCountEntity.copy(count = searchCountEntity.count + 1L))
        }
    }

    fun isUpdating(): Boolean {
        return threadPool.activeCount != 0 || threadPool.queue.isNotEmpty()
    }
}
package io.github.pacdouglas.starwarssearch

import io.github.pacdouglas.starwarssearch.controller.SearchApiController
import io.github.pacdouglas.starwarssearch.model.StarWarsInfoSearchCount
import io.github.pacdouglas.starwarssearch.repository.StarWarsInfoSearchCountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@SpringBootTest
class StarwarsSearchApplicationTests @Autowired constructor(private val controller: SearchApiController,
															private val searchCountRepo: StarWarsInfoSearchCountRepository) {
	@Test
	fun `Testing similar string search`() {
		assertEquals("Luke Skywalker", controller.findType("people", "Luki Skywalki").found["name"])
		assertEquals("Luke Skywalker", controller.findType("people", "Luqui Skywalker").found["name"])
		assertEquals("Luke Skywalker", controller.findType("people", "Luke Sky").found["name"])
	}

	@Test
	fun `Testing the count of searched terms`() {
		searchCountRepo.deleteAll()

		find("people", "Luke Sky", 5)
		find("people", "Darth Vader", 5)
		find("people", "Han Solo", 5)

		Thread.sleep(1000) // The metrics run in parallel

		assertEquals(listOf(5L, 5L, 5L), getMetrics().map { it.count })
	}

	@Test
	fun `Testing if the count of searched terms are sorted by count`() {
		searchCountRepo.deleteAll()

		find("people", "Luke Sky", 5)
		find("people", "Darth Vader", 10)
		find("planets", "Tholoth", 15)
		find("people", "Han Solo", 2)

		Thread.sleep(1000) // The metrics run in parallel

		assertEquals(listOf("Tholoth", "Darth Vader", "Luke Sky", "Han Solo"), getMetrics().map { it.searchedName })
	}

	@Test
	fun `Testing multiple requests and the count feature`() {
		searchCountRepo.deleteAll()

		val pool = Executors.newFixedThreadPool(100)
		repeat(10000) {
			pool.submit {
				println("Searching from thread: ${Thread.currentThread().name}}")
				controller.findType("people", "Han Solo")
			}
		}
		pool.shutdown()
		pool.awaitTermination(10, SECONDS)

		Thread.sleep(7000) // Waiting the update of metrics

		assertEquals(10000, getMetrics().firstOrNull()?.count)
	}

	private fun getMetrics(): List<StarWarsInfoSearchCount> {
		return controller.findMetrics(0).content.toList()
	}

	private fun find(type: String, name: String, repeat: Int) {
		repeat(repeat) {
			controller.findType(type, name)
		}
	}
}

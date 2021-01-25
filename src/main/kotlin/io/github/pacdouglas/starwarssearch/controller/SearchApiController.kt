package io.github.pacdouglas.starwarssearch.controller

import io.github.pacdouglas.starwarssearch.model.StarWarsInfoSearchCount
import io.github.pacdouglas.starwarssearch.model.StarwarsInfo
import io.github.pacdouglas.starwarssearch.repository.StarWarsInfoSearchCountRepository
import io.github.pacdouglas.starwarssearch.service.StarwarsSearchInfoService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/starwars")
class SearchApiController(private val rawDataService: StarwarsSearchInfoService,
                          private val searchCountRepo: StarWarsInfoSearchCountRepository) {
    @GetMapping("/search/{type}", produces = ["application/json"])
    fun findType(@PathVariable type: String, @RequestParam name: String): StarwarsInfo {
        return rawDataService.find(type, name)
    }

    @GetMapping("/metrics")
    fun findMetrics(@RequestParam page: Int): Page<StarWarsInfoSearchCount> {
        return searchCountRepo.findAll(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "count")))
    }
}
package io.github.pacdouglas.starwarssearch.repository

import io.github.pacdouglas.starwarssearch.model.StarWarsInfoSearchCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

interface StarWarsInfoSearchCountRepository : CrudRepository<StarWarsInfoSearchCount, String> {
    fun findAll(pageable: Pageable): Page<StarWarsInfoSearchCount>
}
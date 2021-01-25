package io.github.pacdouglas.starwarssearch.repository

import io.github.pacdouglas.starwarssearch.model.SwApiRawData
import org.springframework.data.repository.CrudRepository

interface SwApiRawDataRepository : CrudRepository<SwApiRawData, String>
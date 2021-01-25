package io.github.pacdouglas.starwarssearch.model

data class StarwarsInfo(
        val found: Map<String, Any?>,
        val similar: List<Map<String, Any?>>
)
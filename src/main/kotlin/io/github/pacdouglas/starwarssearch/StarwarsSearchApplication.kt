package io.github.pacdouglas.starwarssearch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StarwarsSearchApplication

fun main(args: Array<String>) {
    runApplication<StarwarsSearchApplication>(*args)
}

package io.github.pacdouglas.starwarssearch

import io.github.pacdouglas.starwarssearch.repository.SwApiRawDataRepository
import io.github.pacdouglas.starwarssearch.service.PopulateDatabase
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object ApplicationStatus {
    val isReady = AtomicBoolean(false)
}

@SpringBootApplication
class StarwarsSearchApplication {
    @Bean
    fun init(rest: RestTemplate, rawDataRepo: SwApiRawDataRepository): CommandLineRunner {
        return CommandLineRunner {
            PopulateDatabase(rest, rawDataRepo).run()
            ApplicationStatus.isReady.set(true)
            LoggerFactory.getLogger(this::class.java).info("application is ready")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<StarwarsSearchApplication>(*args)
}

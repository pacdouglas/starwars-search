package io.github.pacdouglas.starwarssearch.conf

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
                .setReadTimeout(Duration.ofMinutes(5L))
                .setConnectTimeout(Duration.ofMinutes(5L))
                .build()
    }
}
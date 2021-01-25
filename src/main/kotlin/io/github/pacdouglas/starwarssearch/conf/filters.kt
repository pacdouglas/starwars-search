package io.github.pacdouglas.starwarssearch.conf

import io.github.pacdouglas.starwarssearch.ApplicationStatus
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApplicationFilters {
    @Bean
    fun loggingFilter(): FilterRegistrationBean<LoggingFilter> {
        return FilterRegistrationBean<LoggingFilter>().apply {
            this.filter = LoggingFilter()
            this.addUrlPatterns("/*")
        }
    }

    @Bean
    fun appReadyFilter(): FilterRegistrationBean<ApplicationReadyFilter> {
        return FilterRegistrationBean<ApplicationReadyFilter>().apply {
            this.filter = ApplicationReadyFilter()
            this.addUrlPatterns("/*")
        }
    }
}

@Component
@Order(1)
class LoggingFilter : Filter {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as? HttpServletRequest
        val response = res as? HttpServletResponse

        logger.debug("Logging Request {}: {}", request?.method, request?.requestURI)
        chain.doFilter(req, res)
        logger.debug("Logging Response: status [{}]", response?.status)
    }
}

@Component
@Order(2)
class ApplicationReadyFilter : Filter {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        if (!ApplicationStatus.isReady.get()) {
            val httpServletResponse = res as? HttpServletResponse

            httpServletResponse?.status = HttpStatus.SC_SERVICE_UNAVAILABLE
            httpServletResponse?.writer?.use {
                it.write("The application is not ready yet. Try again in a couple of seconds =)")
                it.flush()
            }

            return
        }

        chain.doFilter(req, res)
    }
}
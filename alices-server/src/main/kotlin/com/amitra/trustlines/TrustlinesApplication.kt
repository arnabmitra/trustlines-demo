package com.amitra.trustlines

import com.amitra.trustlines.service.TrustlineService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.beans.factory.annotation.Autowired


@SpringBootApplication
class TrustlinesApplication


fun main(args: Array<String>) {
    runApplication<TrustlinesApplication>(*args)
}

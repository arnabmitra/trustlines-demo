package com.amitra.trustlines.config

import com.amitra.trustlines.model.extensions.configureTrust
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AppConfig
{
    @Primary
    @Bean
    fun mapper(): ObjectMapper {
        return ObjectMapper().configureTrust()
    }
}

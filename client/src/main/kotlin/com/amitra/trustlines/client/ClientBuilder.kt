package com.amitra.trustlines.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.Request
import feign.Retryer
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder

//common stuff irl
fun ObjectMapper.configure(): ObjectMapper = registerKotlinModule()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

val mapper = ObjectMapper().configure()

class ClientBuilder(url: String, connectTimeOutmillis: Int, readTimeoutMillis: Int) {
    val client: Client = Feign.builder()
            .encoder(JacksonEncoder(mapper))
            .decoder(JacksonDecoder(mapper))
            .errorDecoder(ExceptionDecoder())
            .options(Request.Options(connectTimeOutmillis, readTimeoutMillis))
            .retryer(Retryer.NEVER_RETRY)
            .target(Client::class.java, url)
}

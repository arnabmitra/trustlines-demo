package com.amitra.trustlines.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.ModelRef
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        val uuidParameter = ParameterBuilder()
                .name("uuid")
                .parameterType("header")
                .modelRef(ModelRef("string"))
                .description("UUID of the user")
                .build()

        val uuidParameter1 = ParameterBuilder()
                .name("user-key") // signed key
                .parameterType("header")
                .modelRef(ModelRef("string"))
                .description("UUID of the user")
                .build()

        return Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(listOf(uuidParameter, uuidParameter1))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.amitra.trustlines.web"))
                .paths(PathSelectors.any())
                .build()
    }
}

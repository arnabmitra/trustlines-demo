package com.amitra.trustlines.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull



@ConfigurationProperties(prefix = "trustline-app")
@Validated
class AppProperties {
    @NotNull
    lateinit var userId:String

    @NotNull
    lateinit var userName:String

    @NotNull
    lateinit var userPublicKey: String
    @NotNull
    lateinit var userPrivateKey: String

    @NotNull
    lateinit var networkPubKey: String

    @NotNull
    lateinit var networkPrivateKey: String
}

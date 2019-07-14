package com.amitra.trustlines.web

import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {
    /**
     *Get TrustLine balance
     */
    @GetMapping
    @ResponseBody
    @ApiOperation(value = "health")
    fun health(): Boolean {
        return true
    }

}

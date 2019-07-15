package com.amitra.trustlines.web

import com.amitra.trustlines.client.Client
import com.amitra.trustlines.client.ClientBuilder
import com.amitra.trustlines.model.TrustlineTransfer
import com.amitra.trustlines.model.TrustlineTransferResponse
import com.amitra.trustlines.model.TrustlineTransferWithSig
import com.amitra.trustlines.service.TrustlineService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/trustlines")
@Api(value = "Trustlines Demo", produces = "application/json",
        consumes = "application/json",
        tags = ["TrustLines"])
class TrustlinesController(private val trustlineService: TrustlineService) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    val USER_KEY_HEADER = "user-key"

    private val clients = ConcurrentHashMap<UUID,Client>()

    /**
     *Get TrustLine balance
     */
    @GetMapping("/balance")
    @ResponseBody
    @ApiOperation(value = "Get TrustlineTransfer Balance")
    fun getTrustLine(): BigDecimal {
        return trustlineService.getBalance()
    }

    /**
     *Transfer Token
     */
    @PostMapping("/transfer/approval")
    @ResponseBody
    @ApiOperation(value = "Transfers money out of Account approval..")
    fun approval(@RequestBody trustLineTransfer: TrustlineTransfer, httpServletRequest: HttpServletRequest): TrustlineTransferResponse {
        return trustlineService.approveTransfer(trustLineTransfer)
    }

    /**
     *Transfer Token
     * return transactionId if successful :shrug:
     */
    @PostMapping("/transfer/pay")
    @ResponseBody
    @ApiOperation(value = "Transfers money out of Account..")
    fun transfer(@RequestBody trustLineTransfer: TrustlineTransferWithSig, httpServletRequest: HttpServletRequest): UUID {

        val client =  clients.getOrPut(trustLineTransfer.toEntityUUID) {ClientBuilder("http://localhost:${trustLineTransfer.toEntityPort}/${trustLineTransfer.toEntityName.toLowerCase()}", 10 * 1000, 120 * 1000).client}

        return trustlineService.performTransfer(trustLineTransfer,client)
    }

    @PostMapping("/transfer/acceptance")
    @ResponseBody
    @ApiOperation(value = "Transfers money into an Account..")
    fun accept(@RequestBody trustLineTransfer: TrustlineTransferWithSig, httpServletRequest: HttpServletRequest): UUID {
        return trustlineService.acceptTransfer(trustLineTransfer)
    }
}

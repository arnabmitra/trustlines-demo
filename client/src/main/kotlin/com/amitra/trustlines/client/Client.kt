package com.amitra.trustlines.client

import com.amitra.trustlines.model.TrustlineTransfer
import com.amitra.trustlines.model.TrustlineTransferResponse
import com.amitra.trustlines.model.TrustlineTransferWithSig
import feign.Headers
import feign.Param
import feign.RequestLine
import java.math.BigDecimal
import java.util.UUID


interface Client {
    @Headers(value = ["uuid: {uuid}", "user-key: {userKey}", "Content-Type: application/json"])
    @RequestLine("GET /api/v1/trustlines/balance")
    fun getTrustLineBalance(@Param("uuid") uuid: UUID, @Param("userKey") userkey: String): BigDecimal

    @Headers(value = ["uuid: {uuid}", "user-key: {userKey}", "Content-Type: application/json"])
    @RequestLine("POST /api/v1/trustlines/transfer/approval")
    fun trustlineApproval(@Param("uuid") uuid: UUID, @Param("userKey") userkey: String, trustlineTransfer: TrustlineTransfer): TrustlineTransferResponse

    @Headers(value = ["uuid: {uuid}", "user-key: {userKey}", "Content-Type: application/json"])
    @RequestLine("POST /api/v1/trustlines/transfer/pay")
    fun transferOutOfPayer(@Param("uuid") uuid: UUID, @Param("userKey") userkey: String,
                           trustlineTransfer: TrustlineTransferWithSig): String

    @Headers(value = ["uuid: {uuid}", "user-key: {userKey}", "Content-Type: application/json"])
    @RequestLine("POST /api/v1/trustlines/transfer/acceptance")
    fun sendToPayee(@Param("uuid") uuid: UUID, @Param("userKey") userkey: String, trustlineTransfer: TrustlineTransferWithSig): String

}



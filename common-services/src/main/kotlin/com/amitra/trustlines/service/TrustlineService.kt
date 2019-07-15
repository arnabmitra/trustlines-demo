package com.amitra.trustlines.service

import com.amitra.trustlines.client.Client
import com.amitra.trustlines.model.TrustlineTransfer
import com.amitra.trustlines.model.TrustlineTransferResponse
import com.amitra.trustlines.model.TrustlineTransferWithSig
import com.amitra.trustlines.properties.AppProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.signature.PublicKeySignFactory
import com.google.crypto.tink.signature.PublicKeyVerifyFactory
import com.google.crypto.tink.subtle.Base64
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Service
@EnableConfigurationProperties(value = [AppProperties::class])
class TrustlineService(val appProperties: AppProperties, val objectMapper: ObjectMapper) : ApplicationListener<ApplicationReadyEvent> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        TinkConfig.register()

        logger.info("Welcome to the TrustlineTransfer")
        logger.info("TrustlineTransfer balance is: ${this.getBalance()}")
    }


    @Volatile
    private var trustLineBalance = BigDecimal.ZERO

    private val transactionsTransferred = ConcurrentHashMap.newKeySet<UUID>()

    fun getBalance(): BigDecimal {
        return trustLineBalance
    }

    fun updateBalance(amount: BigDecimal) {
        synchronized(this) {
            trustLineBalance = trustLineBalance.add(amount)
        }
    }

    /**
     * Approve transfer
     */
    fun approveTransfer(trustlineTransfer: TrustlineTransfer): TrustlineTransferResponse {
        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(appProperties.userPrivateKey))

        val signer = PublicKeySignFactory.getPrimitive(keysetHandle)

        val transferSignKey = Base64.encode(signer.sign(objectMapper.writeValueAsBytes(trustlineTransfer)))
        return trustlineTransfer.toTrustLineResponse(transferSignKey)
    }

    /**
     * Perform Transfer..i.e you are the payer here.. you are reducing your balance
     */
    //cheating here with the network key IRL key may have rotated hence create it again through KMS
    fun performTransfer(trustlineTransferWithSig: TrustlineTransferWithSig, client:Client): UUID {

        //idempotency check here..
        if (transactionsTransferred.contains(trustlineTransferWithSig.transferId)) {
            throw IllegalArgumentException("Transfer already executed..")
        }
        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(appProperties.userPublicKey))

        val verifier = PublicKeyVerifyFactory.getPrimitive(keysetHandle)

        verifier.verify(Base64.decode(trustlineTransferWithSig.signatureOfApproval), objectMapper.writeValueAsBytes(trustlineTransferWithSig.toTrustLine()))
        //update balance..negative amount
        updateBalance(-trustlineTransferWithSig.amount)

        transactionsTransferred.add(trustlineTransferWithSig.transferId)

        logger.info("Execute Client Send from ${trustlineTransferWithSig.fromEntityName} to ${trustlineTransferWithSig.toEntityName}...")
        logger.info("Paying ${trustlineTransferWithSig.amount} to ${trustlineTransferWithSig.toEntityName} !")
        //client ..call ..retry if necessary.. assumption is that orchestrator will retry in this case
        try {
            val trustLinePayeeResponse = client.sendToPayee(trustlineTransferWithSig.toEntityUUID,
                    createNetworkKey(trustlineTransferWithSig.toEntityUUID.toString()), trustlineTransferWithSig)
            //if you uncomment these line below, throws idempotent check exception :success:
//             client.sendToPayee(trustlineTransferWithSig.toEntityUUID,
//                    createNetworkKey(trustlineTransferWithSig.toEntityUUID.toString()), trustlineTransferWithSig)
        }catch (ex:RuntimeException)
        {
            //log and rethrow
            //assumption is that someone will retry this request to the payee(probably the orchestrator).. hence not reversing for now
            logger.error("Send unsuccessful ")
            logger.error("Send unsuccessful with exception",ex)

        }

        logger.info("Sent..")
        logger.info("Trustline balance is: ${this.getBalance()}")
        return trustlineTransferWithSig.transferId

    }

    /**
     * Get more money here
     * Signature in TrustlineTransferResponse should be stored in a data store of choosing so that if a question comes up about a transfer.. then trustline member can
     * tell authority where it came from.. i.e if Alice ever disputes a transaction they can be shown this signature
     * to show that they had a legitimate transfer..if not legitimate trustline owner will have to reverse the transaction..
     *
     */
    fun acceptTransfer(trustlineTransferWithSig: TrustlineTransferWithSig): UUID {
        //idempotency check here..
        if (transactionsTransferred.contains(trustlineTransferWithSig.transferId)) {
            throw IllegalArgumentException("Transfer already executed..")
        }

        updateBalance(trustlineTransferWithSig.amount)
        logger.info("Execute Client Send from ${trustlineTransferWithSig.fromEntityName} to ${trustlineTransferWithSig.toEntityName}...")
        logger.info("You were paid ${trustlineTransferWithSig.amount}!")
        logger.info("Trustline balance is: ${this.getBalance()}")
        transactionsTransferred.add(trustlineTransferWithSig.transferId)
        return trustlineTransferWithSig.transferId
    }

    @PostConstruct
    fun init() {
        //do something here after bean is created...
    }

    /**
     * create network key
     */
    fun createNetworkKey(uuid:String):String{
        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(appProperties.networkPrivateKey))

        val signer = PublicKeySignFactory.getPrimitive(keysetHandle)

        //could use Base64 encoder also here..not sure which one is more performant..
        return Base64.encode(signer.sign(uuid.toByteArray()))
    }
}

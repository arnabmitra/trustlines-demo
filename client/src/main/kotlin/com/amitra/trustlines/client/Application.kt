package com.amitra.trustlines.client

import com.amitra.trustlines.model.TrustlineTransfer
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.signature.PublicKeySignFactory
import com.google.crypto.tink.subtle.Base64
import java.math.BigDecimal
import java.util.UUID

class Application

private val clientAlice = ClientBuilder("http://localhost:8420/alice", 10 * 1000, 120 * 1000).client
private val clientBob = ClientBuilder("http://localhost:8421/bob", 10 * 1000, 120 * 1000).client

val aliceUUID = "877355f0-6548-492f-b66e-d156b39b6af4"
val bobUUID = "953e3bac-cd10-4c37-8716-695c53d90783"


lateinit var userKeyForAliceCalls: String
lateinit var userKeyForBobCalls: String


//orchestrator is owner of these keys ..but IRL probably gets it from some KMS..
val publicKeyKeyInfoJson = """{
    "primaryKeyId": 1474414671,
    "key": [{
        "keyData": {
            "typeUrl": "type.googleapis.com/google.crypto.tink.EcdsaPublicKey",
            "keyMaterialType": "ASYMMETRIC_PUBLIC",
            "value": "EgYIAxACGAIaIF3leWahTnBNpcslBSOoMazPdlNLDLWew3CzwfI9GjeVIiBx47dfcnluYvZv3EVLg1RmFz1ZbyDcpstXYONH/EC0fA=="
        },
        "outputPrefixType": "TINK",
        "keyId": 1474414671,
        "status": "ENABLED"
    }]
}
"""

val privateKeyKeyInfoJson = """{
    "primaryKeyId": 1474414671,
    "key": [{
        "keyData": {
            "typeUrl": "type.googleapis.com/google.crypto.tink.EcdsaPrivateKey",
            "keyMaterialType": "ASYMMETRIC_PRIVATE",
            "value": "EkwSBggDEAIYAhogXeV5ZqFOcE2lyyUFI6gxrM92U0sMtZ7DcLPB8j0aN5UiIHHjt19yeW5i9m/cRUuDVGYXPVlvINymy1dg40f8QLR8GiEAr9z62eF8UvVZbDL3/07LbURRiqbLxcYLfD8h/SfTDGw="
        },
        "outputPrefixType": "TINK",
        "keyId": 1474414671,
        "status": "ENABLED"
    }]
}
"""

fun main(args: Array<String>) {

    TinkConfig.register()

    val keysetHandle = CleartextKeysetHandle.read(
            JsonKeysetReader.withString(privateKeyKeyInfoJson))

    val signer = PublicKeySignFactory.getPrimitive(keysetHandle)

    //could use Base64 encoder also here..not sure which one is more performant..
    userKeyForAliceCalls = Base64.encode(signer.sign(aliceUUID.toByteArray()))
    userKeyForBobCalls = Base64.encode(signer.sign(bobUUID.toByteArray()))

    println("Starting Orchestration!!!")

    val balance = clientAlice.getTrustLineBalance(uuidAlice(), userKeyForAliceCalls)
    println("Balance of Alice's trustline is $balance")

    val balanceBob = clientBob.getTrustLineBalance(uuidBob(), userKeyForBobCalls)
    println("Balance of Bob's trustline is $balanceBob")

    val trustlineTransfer = TrustlineTransfer(transferId = UUID.randomUUID(), amount = BigDecimal("10"),
            fromEntityName = "Alice", fromEntityUUID = uuidAlice(), toEntityName = "Bob", toEntityUUID = UUID.fromString(bobUUID))
    val trustLineResponse = clientAlice.trustlineApproval(uuidAlice(), userKeyForAliceCalls, trustlineTransfer)

    val toSend = trustLineResponse.toTrustLineWithSig(8421)
    val trustLinePayerResponse = clientAlice.transferOutOfPayer(uuidAlice(), userKeyForAliceCalls, toSend)

//    val trustLinePayeeResponse = clientBob.sendToPayee(uuidBob(), userKeyForBobCalls, toSend)


}

private fun uuidAlice() = UUID.fromString(aliceUUID)
private fun uuidBob() = UUID.fromString(bobUUID)



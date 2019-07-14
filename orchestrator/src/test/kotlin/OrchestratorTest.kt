import com.amitra.trustlines.client.ClientBuilder
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.signature.PublicKeySignFactory
import com.google.crypto.tink.subtle.Base64
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.annotation.IfProfileValue
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
class OrchestratorTest {

    val aliceUUID = "877355f0-6548-492f-b66e-d156b39b6af4"


//    val publicKeyKeyInfoJson = """{
//    "primaryKeyId": 1474414671,
//    "key": [{
//        "keyData": {
//            "typeUrl": "type.googleapis.com/google.crypto.tink.EcdsaPublicKey",
//            "keyMaterialType": "ASYMMETRIC_PUBLIC",
//            "value": "EgYIAxACGAIaIF3leWahTnBNpcslBSOoMazPdlNLDLWew3CzwfI9GjeVIiBx47dfcnluYvZv3EVLg1RmFz1ZbyDcpstXYONH/EC0fA=="
//        },
//        "outputPrefixType": "TINK",
//        "keyId": 1474414671,
//        "status": "ENABLED"
//    }]
//}
//"""

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

    private val USER_KEY_HEADER = "user-key"


    lateinit var userKeyForAliceCalls: String

    @Before
    fun before() {
        TinkConfig.register()

        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(privateKeyKeyInfoJson))

        val signer = PublicKeySignFactory.getPrimitive(keysetHandle)

        userKeyForAliceCalls = Base64.encode(signer.sign(aliceUUID.toByteArray()))
    }

    private val client = ClientBuilder("http://localhost:8420/alice", 10 * 1000, 120 * 1000).client



    @Test
    @IfProfileValue(name = "run.integration.tests", value = "true")
    fun orchestrate() {
        val balance = client.getTrustLineBalance(UUID.fromString(aliceUUID), userKeyForAliceCalls)
        assertTrue { balance >= BigDecimal.ZERO }
    }
}

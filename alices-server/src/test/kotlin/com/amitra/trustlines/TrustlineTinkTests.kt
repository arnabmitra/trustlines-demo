package com.amitra.trustlines

import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.signature.PublicKeySignFactory
import com.google.crypto.tink.signature.PublicKeyVerifyFactory
import com.google.crypto.tink.signature.SignatureKeyTemplates
import org.junit.Test
import java.security.GeneralSecurityException
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import org.junit.Before
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.Base64
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class TestEncryption {

    private val DATA = "DANK"
    @Before
    fun before() {
        TinkConfig.register()
    }

    @Test
    @Throws(GeneralSecurityException::class)
    fun givenSignData_thenVerifySignature_generatedKeys() {

        val privateKeysetHandle = KeysetHandle.generateNew(
                SignatureKeyTemplates.ECDSA_P256)

        println("\nPrinting out key:")
        val outputStream = ByteArrayOutputStream()
        CleartextKeysetHandle.write(privateKeysetHandle, JsonKeysetWriter.withOutputStream(outputStream))
        println(String(outputStream.toByteArray()))

        println("\nPrinting out Public key:")

        val publicKeysetHandle = privateKeysetHandle.publicKeysetHandle
        val outputStreamPub = ByteArrayOutputStream()

        CleartextKeysetHandle.write(publicKeysetHandle, JsonKeysetWriter.withOutputStream(outputStreamPub))
        println(String(outputStreamPub.toByteArray()))

        val signer = PublicKeySignFactory.getPrimitive(privateKeysetHandle)

        val signature = signer.sign(DATA.toByteArray())

        //signature
        println(Base64.getEncoder().encode(signature))
        println(signature.toString(Charsets.UTF_8))

        val verifier = PublicKeyVerifyFactory.getPrimitive(publicKeysetHandle)

        assertTrue { verifier.verify(signature, DATA.toByteArray()) == Unit }

    }

    @Test
    @Throws(GeneralSecurityException::class)
    fun givenSignData_thenVerifySignature_withstatic_keys() {
        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString("{\n" +
                        "    \"primaryKeyId\": 37688185,\n" +
                        "    \"key\": [{\n" +
                        "        \"keyData\": {\n" +
                        "            \"typeUrl\": \"type.googleapis.com/google.crypto.tink.EcdsaPrivateKey\",\n" +
                        "            \"keyMaterialType\": \"ASYMMETRIC_PRIVATE\",\n" +
                        "            \"value\": \"Ek0SBggDEAIYAhogdeNsSvo034t/ZJeNUd6DrFnTin3g8iKW9HG3bA6r6mMiIQDpGJwHZs1h92btyjnEscnap+D4Q5lb7Moxdwdik/B7rBogdzL9VuIZW7PL9OXi92qtJLFwZzvodNRMwYfUzmaJmCU=\"\n" +
                        "        },\n" +
                        "        \"outputPrefixType\": \"TINK\",\n" +
                        "        \"keyId\": 37688185,\n" +
                        "        \"status\": \"ENABLED\"\n" +
                        "    }]\n" +
                        "}"))


        val signer1 = PublicKeySignFactory.getPrimitive(keysetHandle)

        val signature1 = signer1.sign(DATA.toByteArray())


        println("\nPrinting out key###:")
        val outputStream1 = ByteArrayOutputStream()
        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withOutputStream(outputStream1))
        println(String(outputStream1.toByteArray()))


        val keysetHandlePublic = CleartextKeysetHandle.read(
                JsonKeysetReader.withString("{\n" +
                        "    \"primaryKeyId\": 37688185,\n" +
                        "    \"key\": [{\n" +
                        "        \"keyData\": {\n" +
                        "            \"typeUrl\": \"type.googleapis.com/google.crypto.tink.EcdsaPublicKey\",\n" +
                        "            \"keyMaterialType\": \"ASYMMETRIC_PUBLIC\",\n" +
                        "            \"value\": \"EgYIAxACGAIaIHXjbEr6NN+Lf2SXjVHeg6xZ04p94PIilvRxt2wOq+pjIiEA6RicB2bNYfdm7co5xLHJ2qfg+EOZW+zKMXcHYpPwe6w=\"\n" +
                        "        },\n" +
                        "        \"outputPrefixType\": \"TINK\",\n" +
                        "        \"keyId\": 37688185,\n" +
                        "        \"status\": \"ENABLED\"\n" +
                        "    }]\n" +
                        "}"))


        println("\nPrinting out key public3###:")
        val outputStream3 = ByteArrayOutputStream()
        CleartextKeysetHandle.write(keysetHandlePublic, JsonKeysetWriter.withOutputStream(outputStream3))
        println(String(outputStream3.toByteArray()))

        val verifier1 = PublicKeyVerifyFactory.getPrimitive(keysetHandlePublic)

        assertTrue { verifier1.verify(signature1, DATA.toByteArray()) == Unit }
        //create another verifier
        val privateKeysetHandle = KeysetHandle.generateNew(
                SignatureKeyTemplates.ECDSA_P256)
        val verifier = PublicKeyVerifyFactory.getPrimitive(privateKeysetHandle.publicKeysetHandle)

        assertFailsWith<GeneralSecurityException> { verifier.verify(signature1, DATA.toByteArray()) }
    }

}

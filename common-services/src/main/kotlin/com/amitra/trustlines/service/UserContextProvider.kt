package com.amitra.trustlines.service

import com.amitra.trustlines.model.TrustLineUser
import com.amitra.trustlines.model.UserRoles
import com.amitra.trustlines.properties.AppProperties
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.signature.PublicKeyVerifyFactory
import com.google.crypto.tink.subtle.Base64
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.security.GeneralSecurityException
import java.util.UUID

interface UserContext {
    fun getContextUser(userId: UUID, userKey: String): TrustLineUser?
    fun getPublicKeySetHandle(userId: UUID): String?
    fun getPrivateKeySetHandle(userId: UUID): String?
}

@Service
@EnableConfigurationProperties(value = [AppProperties::class])
class UserContextProvider(private val appProperties: AppProperties) : UserContext {
    override fun getContextUser(userId: UUID, userKey: String): TrustLineUser? {
        //this should be from some trusted source.. for this demo it is just com.amitra.trustlines.properties..

        //verify that request is meant for this server..
        if (userId.toString() != appProperties.userId) {
            throw org.springframework.security.access.AccessDeniedException("This request is not meant for this server.. UUID mismatch")
        }
        //get from kms IRL
        val keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(appProperties.networkPubKey))

        val verifier = PublicKeyVerifyFactory.getPrimitive(keysetHandle)

        //verify user key is correct..
        try {
            verifier.verify(Base64.decode(userKey), userId.toString().toByteArray())
        } catch (ex: GeneralSecurityException) {
            throw org.springframework.security.access.AccessDeniedException("This request is not meant for this server.. user key mismatch")
        }

        return TrustLineUser(userName = appProperties.userName, userId = UUID.fromString(appProperties.userId),
                role = UserRoles.USER, privateKeyHandle = appProperties.userPrivateKey, publicKeyHandle = appProperties.userPublicKey)

    }

    override fun getPublicKeySetHandle(userId: UUID): String? {
        val userInContext: TrustLineUser? = getUserIncontext()
        return userInContext?.publicKeyHandle
    }

    override fun getPrivateKeySetHandle(userId: UUID): String? {
        val userInContext: TrustLineUser? = getUserIncontext()
        return userInContext?.privateKeyHandle
    }

    fun getUserIncontext(): TrustLineUser? {
        return SecurityContextHolder.getContext().authentication.details as TrustLineUser
    }

}


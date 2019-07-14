package com.amitra.trustlines.auth

import com.amitra.trustlines.model.TrustLineUser
import com.amitra.trustlines.model.UserRoles
import com.amitra.trustlines.service.UserContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.UUID

class UserAuthenticationManager(private val userContextProvider: UserContext) : AuthenticationManager {

    @Throws(Exception::class)
    override fun authenticate(authentication: Authentication?): Authentication {
        if (authentication == null) {
            throw BadCredentialsException("User authentication object cannot be null")
        }
        val memberAuthentication = authentication as UserAuth
        val user = userContextProvider.getContextUser(memberAuthentication.credentials, memberAuthentication.userKey)
                ?: throw UsernameNotFoundException("user enrollment not found")


        memberAuthentication.uuid = user.userId
        memberAuthentication.isAuthenticated = true
        memberAuthentication.setDetails(user)
        return memberAuthentication
    }
}

class UserAuth(var uuid: UUID, var userKey: String) : Authentication {
    private var authenticated = false
    private var trustLineUser: TrustLineUser? = null

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
            MutableList(if (trustLineUser != null) 1 else 0) {
                GrantedAuthority { "ROLE_${trustLineUser?.role}" }
            }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

    override fun getName(): String = uuid.toString()

    override fun getCredentials(): UUID = uuid

    override fun getPrincipal(): UUID = uuid

    override fun isAuthenticated(): Boolean = authenticated

    override fun getDetails(): TrustLineUser? {
        return trustLineUser
    }

    fun hasRole(userRole: UserRoles): Boolean {
        return authorities.contains(GrantedAuthority { "ROLE_$userRole" })
    }

    fun setDetails(trustLineUser: TrustLineUser) {
        this.trustLineUser = trustLineUser
    }

}


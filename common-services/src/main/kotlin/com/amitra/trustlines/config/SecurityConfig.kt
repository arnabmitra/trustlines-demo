package com.amitra.trustlines.config

import com.amitra.trustlines.auth.UserAuth
import com.amitra.trustlines.auth.UserAuthenticationManager
import com.amitra.trustlines.service.UserContextProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter
import java.lang.RuntimeException
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var userContextProvider: UserContextProvider

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable() // JWT doesn't care
                .headers().frameOptions().disable()

        http.authorizeRequests()
                .antMatchers("/**").hasAnyRole("USER")
                .anyRequest()
                .authenticated()
                .and()
                // TODO: add after filter for cycling jwt
                .addFilterBefore(HeaderInterceptFilter(), BasicAuthenticationFilter::class.java)
    }

    @Throws(Exception::class)
    override fun configure(webSecurity: WebSecurity) {
        webSecurity
                .ignoring()
                .antMatchers("/health/**")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                .antMatchers("/swagger-resources/**")
                .antMatchers("/swagger-ui.html")
    }

    @Throws(Exception::class)
    override fun authenticationManager(): AuthenticationManager {
        /*TODO this needs to be thought through - since the user x-uuid and x-roles are proxied by the
            Identity service, how will they relate to the Network Enrollments/MSP?  Will require
            an initial user and a Network App administrator to register/enroll the user.  Then
            a tie-in to the Identity service?  Or Identity service sign up --> Network App register/enroll with UUID?
        */
        return UserAuthenticationManager(userContextProvider)
    }
}

private const val UUID_HEADER = "uuid"
private const val USER_KEY_HEADER = "user-key"

class HeaderInterceptFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val uuid = UUID.fromString(request.getHeader(UUID_HEADER))
        val userKey = request.getHeader(USER_KEY_HEADER)
        SecurityContextHolder.getContext().authentication = UserAuth(uuid, userKey)
        filterChain.doFilter(request, response)
    }

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return "/health" == request.servletPath
    }
}

fun currentMemberId() =
        (SecurityContextHolder.getContext().authentication as UserAuth?)?.uuid
                ?: throw RuntimeException("No user found (no UUID provided)")

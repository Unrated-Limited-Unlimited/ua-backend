package com.ulu.security

import com.ulu.models.UserData
import com.ulu.repositories.UserDataRepository
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider
import jakarta.inject.Singleton

/**
 * Validates authentication requests sent to POST /login
 * */
@Singleton
class LoginProvider<B>(private val userDataRepository: UserDataRepository) : HttpRequestAuthenticationProvider<B> {

    override fun authenticate(
        httpRequest: HttpRequest<B>?,
        authenticationRequest: AuthenticationRequest<String, String>
    ): AuthenticationResponse {
        val userData: UserData? = userDataRepository.getUserDataByName(authenticationRequest.identity)
        return if (userData != null && AccountCreationService().checkPassword(
                authenticationRequest.secret,
                userData.password
            )
        ) {
            AuthenticationResponse.success(authenticationRequest.identity, userData.roles)
        } else {
            AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)
        }
    }
}
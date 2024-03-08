package com.ulu.security

import com.ulu.repositories.UserDataRepository
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider
import jakarta.inject.Singleton

/**
 * Validates authentication requests sent to POST /login
 * */
@Singleton
class AuthProvider<B>(private val userDataRepository: UserDataRepository) : HttpRequestAuthenticationProvider<B> {

    override fun authenticate(
        httpRequest: HttpRequest<B>?,
        authenticationRequest: AuthenticationRequest<String, String>
    ): AuthenticationResponse {
        return if (userDataRepository.getUserDataByNameAndPassword(authenticationRequest.identity,authenticationRequest.secret) != null){
            AuthenticationResponse.success(authenticationRequest.identity)
        }
        else{
            AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)
        }
    }
}
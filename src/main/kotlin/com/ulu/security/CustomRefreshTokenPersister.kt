package com.ulu.security

import com.ulu.models.RefreshToken
import com.ulu.repositories.JwtRefreshTokenRepository
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent
import io.micronaut.security.token.refresh.RefreshTokenPersistence
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

/**
 * Stores created refresh tokens in JPA repository.
 *
 * Verifies JWT refresh tokens sent to POST /oauth/access_token with a:
 * {
 *   "refresh_token": "eyJhb...",
 *   "grant_type": "refresh_token"
 * }
 * */
@Singleton
class RefreshTokenPersistence(private val jwtRefreshTokenRepository: JwtRefreshTokenRepository) :
    RefreshTokenPersistence {

    override fun persistToken(event: RefreshTokenGeneratedEvent?) {
        if (event?.refreshToken != null && event.authentication?.name != null) {
            val jwtRefreshToken = RefreshToken(
                username = event.authentication.name,
                refreshToken = event.refreshToken,
                revoked = false
            )
            jwtRefreshTokenRepository.save(jwtRefreshToken)
        }
    }

    override fun getAuthentication(refreshToken: String): Publisher<Authentication> {
        return Flux.create({ emitter: FluxSink<Authentication> ->
            val jwtRefreshToken: RefreshToken? = jwtRefreshTokenRepository.findByRefreshToken(refreshToken)
            if (jwtRefreshToken != null) {
                if (jwtRefreshToken.revoked) {
                    emitter.error(OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null))
                } else {
                    emitter.next(Authentication.build(jwtRefreshToken.username))
                    emitter.complete()
                }
            }
        }, FluxSink.OverflowStrategy.ERROR)
    }
}

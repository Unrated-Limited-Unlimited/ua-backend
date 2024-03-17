package com.ulu.security

import com.ulu.models.JwtRefreshToken
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
import java.util.Base64

/**
 * Stores created refresh tokens in JPA repository.
 *
 * Persists tokens when /login or /oauth/access_token is used.
 * */
@Singleton
class JwtPersistenceProvider(private val jwtRefreshTokenRepository: JwtRefreshTokenRepository) :
    RefreshTokenPersistence {

    override fun persistToken(event: RefreshTokenGeneratedEvent?) {
        if (event?.refreshToken != null && event.authentication?.name != null) {
            val jwtRefreshToken = JwtRefreshToken(
                username = event.authentication.name,
                refreshToken = event.refreshToken,
                revoked = false
            )
            jwtRefreshTokenRepository.saveAndFlush(jwtRefreshToken)
        }
    }

    override fun getAuthentication(refreshToken: String): Publisher<Authentication> {
        return Flux.create({ emitter: FluxSink<Authentication> ->
            val jwtRefreshToken: JwtRefreshToken? = jwtRefreshTokenRepository.getByRefreshToken(refreshToken)
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

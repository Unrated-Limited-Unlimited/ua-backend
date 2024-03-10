package com.ulu.security.filters

import com.ulu.repositories.JwtRefreshTokenRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.token.render.BearerAccessRefreshToken
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.util.Base64

/**
 * Filter class for intercepting /login and /oauth/access_token success responses
 *
 * Adds jwt access token to the response Authorization header.
 * Saves the jwt as a child of the stored refresh token.
 * */
@Filter(patterns = ["/login", "/oauth/access_token"])
class AuthJwtFilter(private val jwtRefreshTokenRepository: JwtRefreshTokenRepository) : HttpServerFilter {

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return Mono.from(chain.proceed(request))
            .map { response ->
                if (response.status.code == 200 && response.body.isPresent && response.body.get() is BearerAccessRefreshToken) {
                    val authentication = response.body.get() as BearerAccessRefreshToken
                    val jwt = authentication.accessToken

                    // Refresh token is base64 encoded as {"alg":"HS256"}.token.secret, so we need to extract the token and decode it
                    val extractedToken = authentication.refreshToken.split(".")[1]
                    val decodedRefreshToken = String(Base64.getUrlDecoder().decode(extractedToken))

                    // Save token as a child of refresh token
                    val jwtRefreshToken = jwtRefreshTokenRepository.getByRefreshToken(decodedRefreshToken)
                    if (jwtRefreshToken?.id != null) {
                        jwtRefreshTokenRepository.addAccessTokenToJwtRefreshToken(jwtRefreshToken.id, jwt)
                    }

                    // Add access token as Authorization in response header
                    response.headers.add("Authorization", "Bearer $jwt")
                }
                response
            }
    }
}

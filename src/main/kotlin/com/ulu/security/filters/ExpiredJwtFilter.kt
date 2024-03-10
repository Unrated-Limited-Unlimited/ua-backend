package com.ulu.security.filters

import com.ulu.models.JwtRefreshToken
import com.ulu.repositories.JwtRefreshTokenRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.token.render.BearerAccessRefreshToken
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Filter class for intercepting all authenticated /graphql requests
 *
 * If the provided jwt token is marked as expired (from logging out/changing password) the request becomes invalid.
 * */
@Filter("/graphql")
class ExpiredJwtFilter(private val jwtRefreshTokenRepository: JwtRefreshTokenRepository) : HttpServerFilter {

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        if (request.headers.contains("Authorization")){
            // Remove "Bearer " from access token
            val jwt = request.headers["Authorization"].split(" ")[1]
            val jwtRefreshToken : JwtRefreshToken? = jwtRefreshTokenRepository.findByAccessToken(jwt)

            // Filter out marked token requests
            if (jwtRefreshToken != null && jwtRefreshTokenRepository.getRevokedByRefreshToken(jwtRefreshToken.refreshToken)){
                return Flux.just(HttpResponse.unauthorized<Any>().body("Invalid JWT Token"))
            }
        }
        return Mono.from(chain.proceed(request))
    }
}

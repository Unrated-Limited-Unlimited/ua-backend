package com.ulu.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.token.render.BearerAccessRefreshToken
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * Filter class for intercepting /login success responses, to add jwt access token to the response Authorization header.
 * */
@Filter("/login")
class AuthHeaderFilter : HttpServerFilter {

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return Mono.from(chain.proceed(request))
            .map { response ->
                if (response.status.code == 200 && response.body.isPresent && response.body.get() is BearerAccessRefreshToken) {
                    val authentication = response.body.get() as BearerAccessRefreshToken
                    val jwt = authentication.accessToken
                    response.headers.add("Authorization", "Bearer $jwt")
                }
                response
            }
    }
}

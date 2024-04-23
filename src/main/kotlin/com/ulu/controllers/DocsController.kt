package com.ulu.controllers

import com.ulu.dto.LoginRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.uri.UriBuilder
import io.micronaut.security.annotation.Secured
import io.micronaut.security.endpoints.TokenRefreshRequest
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.net.URI

/**
 * Documentation class for creating OpenAPI docs for endpoints that are pre-implemented.
 * To avoid overshadowing of the real endpoints, a hidden character \u200E is used.
 * */
@Controller
class DocsController {
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/login\u200E")
    @Operation(
        summary = "User login",
        description =
            "Authorize a user with username and password." +
                "\nA JWT token in the form of a cookie is issued upon success.",
    )
    @RequestBody(
        description = "Registration details",
        required = true,
        content = [Content(schema = Schema(implementation = LoginRequest::class))],
    )
    @ApiResponse(responseCode = "200", description = "Login successful.")
    @ApiResponse(responseCode = "401", description = "Bad request if username or password is missing, or invalid user credentials.")
    fun dummyLogin(
        @Body loginRequest: LoginRequest,
    ): HttpResponse<*> {
        // The /login is provided by micronaut security, this is just for OpenAPI documentation.
        return HttpResponse.badRequest("This is a documentation endpoint.")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/oauth/access_token\u200E")
    @Operation(
        summary = "Refresh JWT token",
        description = "Create a new JWT session token by using refresh token.",
    )
    @RequestBody(
        description = "Refresh token details",
        required = true,
        content = [Content(schema = Schema(implementation = TokenRefreshRequest::class))],
    )
    @ApiResponse(responseCode = "200", description = "JWT refresh successful.")
    @ApiResponse(
        responseCode = "400",
        description =
            "Invalid request if missing refresh_token and grant_type." +
                "\nInvalid grant if refresh token is invalid",
    )
    fun dummyOauthAccessToken(
        @Body tokenRefreshRequest: TokenRefreshRequest,
    ): HttpResponse<*> {
        // This is just a dummy for creating docs, actual implementation provided by micronaut security
        return HttpResponse.badRequest("This is a documentation endpoint.")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/graphql\u200E")
    @ApiResponse(responseCode = "200", description = "Query sent.")
    @Operation(
        summary = "GraphQL API",
        description =
            "Endpoint for GraphQL requests." +
                "\nFor further details look at schema.graphqls or use the /graphiql playground to create requests.",
    )
    @RequestBody(required = true)
    fun dummyGraphql(): HttpResponse<*> {
        // This is just a dummy for creating docs for the real /graphql
        return HttpResponse.badRequest("This is a documentation endpoint.")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/")
    @Hidden // Hide from OpenAPI docs
    fun swaggerRedirect(): HttpResponse<*> {
        // Redirect / to /swagger-ui
        val uri: URI = UriBuilder.of("/swagger-ui").path("index.html").build()
        return HttpResponse.seeOther<URI>(uri)
    }
}

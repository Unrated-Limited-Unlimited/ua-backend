package com.ulu.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.uri.UriBuilder
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.net.URI

@Controller
class DocsController {
    data class LoginDTO(
        val username: String,
        val password: String,
    )

    @Post("/login\u200E")
    @Operation(
        summary = "User login",
        description = "Authorize a user with username and password. A JWT token in the form of a cookie is issued upon success.",
    )
    @RequestBody(
        description = "Registration details",
        required = true,
        content = [Content(schema = Schema(implementation = LoginDTO::class))],
    )
    @ApiResponse(responseCode = "200", description = "Login successful.")
    @ApiResponse(responseCode = "401", description = "Bad request if username or password is missing, or invalid user credentials.")
    fun dummyLogin() {
        // The /login is provided by micronaut security, this is just for OpenAPI documentation.
    }

    @Post("/graphql\u200E")
    @ApiResponse(responseCode = "200", description = "Query sent.")
    @Operation(
        summary = "GraphQL API",
        description = "Endpoint for GraphQL requests. For further details look at schema.graphqls or use /graphiql to create requests.",
    )
    fun dummyGraphql() {
        // This is just a dummy for creating docs for the real /graphql
    }

    @Get("/")
    @Hidden
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun swaggerRedirect(): HttpResponse<*> {
        val uri: URI = UriBuilder.of("/swagger-ui").path("index.html").build()
        return HttpResponse.seeOther<URI>(uri)
    }
}

package com.ulu.controllers

import com.ulu.dto.RegisterRequest
import com.ulu.services.AccountService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.utils.DefaultSecurityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

/**
 * Normal REST controller for user authentication.
 * The path "/login" is provided by micronaut-security-jwt
 * JSON body:
 * {
 *      "username": "John",
 *      "password": "123"
 * }
 *
 * Returns a JWT token as well as a refresh token that can be used in
 * */
@Controller
class AuthController(
    private val securityService: DefaultSecurityService,
    private val accountService: AccountService,
) {
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/logout")
    @Operation(summary = "Logout", description = "Invalidates all created refresh_tokens.")
    @ApiResponse(responseCode = "200", description = "All sessions logged out")
    fun logout(): HttpResponse<*> {
        // Invalidates all sessions.
        accountService.invalidateSessions(securityService.authentication.get().name)

        // Max age set to 0 to expire immediately
        val emptyJwtCookie = Cookie.of("JWT", "").maxAge(0)
        val emptyJwtRefreshCookie = Cookie.of("JWT_REFRESH_TOKEN", "").path("/oauth/access_token").maxAge(0)

        return HttpResponse.ok("All sessions logged out!").cookie(emptyJwtCookie).cookie(emptyJwtRefreshCookie)
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register")
    @Operation(summary = "User Registration", description = "Registers a new user with the provided details.")
    @RequestBody(
        description = "Registration details",
        required = true,
        content = [Content(schema = Schema(implementation = RegisterRequest::class))],
    )
    @ApiResponse(responseCode = "201", description = "New account created.")
    @ApiResponse(responseCode = "400", description = "Bad request if username is taken, password is insecure, or email is invalid.")
    fun register(
        @Body registerData: RegisterRequest,
    ): HttpResponse<*> {
        return when (val result = accountService.registerNewAccount(registerData)) {
            is AccountService.AccountCreationResult.Success -> HttpResponse.created(result.message)
            is AccountService.AccountCreationResult.Failure -> HttpResponse.badRequest(result.error)
        }
    }
}

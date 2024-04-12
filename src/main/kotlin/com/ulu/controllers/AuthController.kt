package com.ulu.controllers

import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.services.AccountCreationService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
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
    private val userDataRepository: UserDataRepository,
    private val jwtRefreshTokenRepository: JwtRefreshTokenRepository,
) {
    data class RegisterDTO(
        val username: String,
        val password: String,
        val email: String,
        val img: String?,
    )

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/logout")
    @Operation(summary = "Logout", description = "Invalidates all created refresh_tokens.")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    fun logout(): HttpResponse<*> {
        // Invalidates all sessions.
        jwtRefreshTokenRepository.updateRevokedByUsername(securityService.authentication.get().name, true)
        return HttpResponse.ok("All sessions logged out!")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register")
    @Operation(summary = "User Registration", description = "Registers a new user with the provided details.")
    @RequestBody(
        description = "Registration details",
        required = true,
        content = [Content(schema = Schema(implementation = RegisterDTO::class))],
    )
    @ApiResponse(responseCode = "201", description = "New account created.")
    @ApiResponse(responseCode = "400", description = "Bad request if username is taken, password is insecure, or email is invalid.")
    fun register(
        @Body registerData: RegisterDTO,
    ): HttpResponse<*> {
        if (userDataRepository.existsByName(registerData.username)) {
            return HttpResponse.badRequest("Username already taken!")
        }
        if (!AccountCreationService().isValidPassword(registerData.password)) {
            return HttpResponse.badRequest("Unsecure password provided.")
        }
        if (!AccountCreationService().isValidEmail(registerData.email)) {
            return HttpResponse.badRequest("Invalid email provided.")
        }

        // Create new account
        val userData =
            UserData(
                name = registerData.username,
                password = AccountCreationService().hashPassword(registerData.password),
                email = registerData.email,
                img = registerData.img,
            )
        userDataRepository.save(userData)

        return HttpResponse.created("New account created.")
    }
}

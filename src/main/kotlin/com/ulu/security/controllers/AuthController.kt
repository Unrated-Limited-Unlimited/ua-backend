package com.ulu.security.controllers

import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.security.AccountCreationService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.utils.DefaultSecurityService

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
    private val jwtRefreshTokenRepository: JwtRefreshTokenRepository
) {
    // Default /login uses username instead of name
    data class RegisterDTO(
        val username: String,
        val password: String,
        val email: String,
        val img: String?
    )

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/logout")
    fun logout(request: HttpRequest<*>): HttpResponse<*> {
        // Invalidates all sessions.
        jwtRefreshTokenRepository.updateRevokedByUsername(securityService.authentication.get().name, true)
        return HttpResponse.ok("All sessions logged out!")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register")
    fun register(@Body registerData: RegisterDTO): HttpResponse<*> {
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
        val userData = UserData(
            name = registerData.username,
            password = AccountCreationService().hashPassword(registerData.password),
            email = registerData.email,
            img = registerData.img
        )
        userDataRepository.save(userData)

        return HttpResponse.created("New account created.")
    }
}

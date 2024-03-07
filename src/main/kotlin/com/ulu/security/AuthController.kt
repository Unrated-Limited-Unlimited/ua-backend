package com.ulu.security

import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
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
    fun logout(securityService: DefaultSecurityService): HttpResponse<*> {
        jwtRefreshTokenRepository.updateRevokedByUsername(securityService.authentication.get().name, true)
        return HttpResponse.ok("Logged out!")
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/register")
    fun register(@Body registerData: RegisterDTO): HttpResponse<*> {
        if (userDataRepository.existsByName(registerData.username)) {
            return HttpResponse.badRequest("Username taken!")
        }
        //TODO: add signup requirements, password length, email validation, etc.
        //TODO: Use bcrypt on password.
        // Create new user and save it
        val userData = UserData(name = registerData.username, password = registerData.password, email = registerData.email, img = registerData.img)
        return HttpResponse.ok(userDataRepository.save(userData))
    }
}
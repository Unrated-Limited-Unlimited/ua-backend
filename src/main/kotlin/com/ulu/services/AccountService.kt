package com.ulu.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ulu.dto.RegisterRequest
import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import jakarta.inject.Singleton

/**
 * Singleton class for handling user registration.
 *
 * Random salt will be added to the passwords internally by BCrypt.
 * */
private const val MIN_PASSWORD_LENGTH = 8

@Singleton
class AccountService(
    private val userDataRepository: UserDataRepository,
    private val jwtRefreshTokenRepository: JwtRefreshTokenRepository,
    private val requestValidatorService: RequestValidatorService,
) {
    sealed class AccountCreationResult {
        data class Success(val message: String, val userData: UserData) : AccountCreationResult()

        data class Failure(val error: String) : AccountCreationResult()
    }

    /**
     * Register a new account.
     * */
    fun registerNewAccount(registerData: RegisterRequest): AccountCreationResult {
        if (userDataRepository.existsByName(registerData.username)) {
            return AccountCreationResult.Failure("Username already taken!")
        }
        if (!isValidPassword(registerData.password)) {
            return AccountCreationResult.Failure("Unsecure password provided.")
        }
        if (!isValidEmail(registerData.email)) {
            return AccountCreationResult.Failure("Invalid email provided.")
        }
        requestValidatorService.verifyMinLength(registerData.username, 1)

        val userData =
            UserData(
                name = registerData.username,
                password = hashPassword(registerData.password),
                email = registerData.email,
            )
        return AccountCreationResult.Success("New account created.", userDataRepository.save(userData))
    }

    fun invalidateSessions(username: String) {
        jwtRefreshTokenRepository.updateRevokedByUsername(username, true)
    }

    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    fun checkPassword(
        plainPassword: String,
        hashedPassword: String,
    ): Boolean {
        val result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword)
        return result.verified
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.+[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    fun isValidPassword(plainPassword: String): Boolean {
        return plainPassword.length >= MIN_PASSWORD_LENGTH
    }
}

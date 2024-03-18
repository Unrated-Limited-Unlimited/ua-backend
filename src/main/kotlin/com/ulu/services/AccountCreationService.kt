package com.ulu.services

import at.favre.lib.crypto.bcrypt.BCrypt
import jakarta.inject.Singleton

/**
 * Singleton class for handling user registration.
 *
 * Random salt will be added to the passwords internally by BCrypt.
 * */
private const val MIN_PASSWORD_LENGTH = 10
@Singleton
class AccountCreationService {

    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        val result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword)
        return result.verified
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.+[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    fun isValidPassword(plainPassword: String) : Boolean {
        return plainPassword.length >= MIN_PASSWORD_LENGTH
    }
}
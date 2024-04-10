package com.ulu.services

import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class RequestValidatorService {
    /**
     * Verify value range of user input.
     * */
    fun verifyScoreRange(
        score: Double,
        minScore: Double = 0.0,
        maxScore: Double = 1.0,
    ) {
        if (score < minScore || score > maxScore) {
            error("INVALID REQUEST: Score range must be between $minScore and $maxScore.")
        }
    }

    /**
     * Verify minLength for user input
     * */
    fun verifyMinLength(
        string: String,
        minLength: Int = 1,
    ) {
        if (string.length < minLength) {
            error("INVALID REQUEST: Input must be at least of length $minLength")
        }
    }

    /**
     * Verify that user is authenticated / Logged in
     * */
    fun verifyAuthenticated(securityService: DefaultSecurityService) {
        if (!securityService.isAuthenticated) {
            error("ACCESS DENIED: Unauthenticated, login to proceed.")
        }
    }

    /**
     * Verify user is authenticated with ROLE_ADMIN
     * */
    fun verifyAdmin(securityService: DefaultSecurityService) {
        verifyAuthenticated(securityService)
        if (!isAdmin(securityService)) {
            error("ACCESS DENIED: This action requires admin permission!")
        }
    }

    /**
     * Check if user has admin role.
     * @return True || False
     * */
    fun isAdmin(securityService: DefaultSecurityService): Boolean {
        return securityService.authentication.get().roles.contains("ROLE_ADMIN")
    }
}

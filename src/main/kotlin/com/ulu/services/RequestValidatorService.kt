package com.ulu.services

import graphql.schema.DataFetchingEnvironment
import io.micronaut.data.model.Pageable
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton
import kotlin.math.min

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

    fun getPaging(environment: DataFetchingEnvironment): Pageable {
        var page = 0
        var size = 10

        // Get page and size from input
        val pagingInput = environment.getArgument<Map<*, *>>("paging")
        if (pagingInput != null) {
            page = pagingInput["page"] as Int
            size = pagingInput["size"] as Int
        }
        return Pageable.from(page, size)
    }

    /**
     * Convert list to paged list
     * */
    fun <T> getPage(
        list: List<T>,
        pageable: Pageable,
    ): List<T> {
        // Calculate the index range for the requested page
        val fromIndex = pageable.number * pageable.size

        // Constraint paging with max size of list
        val toIndex = min(fromIndex + pageable.size, list.size)

        // Check if fromIndex is within list size
        return if (fromIndex < list.size) {
            list.subList(fromIndex, toIndex)
        } else {
            emptyList()
        }
    }
}

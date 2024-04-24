package com.ulu.repositories

import com.ulu.models.Rating
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface RatingRepository : JpaRepository<Rating, Long> {
    fun existsByWhiskeyIdAndUserId(
        whiskeyId: Long,
        userId: Long,
    ): Boolean

    fun findByWhiskeyIdAndUserId(
        whiskeyId: Long,
        userId: Long,
    ): Rating
}

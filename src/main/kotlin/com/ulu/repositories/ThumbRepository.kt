package com.ulu.repositories

import com.ulu.models.Thumb
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ThumbRepository : JpaRepository<Thumb,Long> {

    fun existsByUserIdAndRatingId(userId: Long, ratingId: Long) : Boolean
}
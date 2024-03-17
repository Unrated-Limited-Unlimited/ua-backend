package com.ulu.repositories

import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ThumbRepository : JpaRepository<Thumb,Long> {

    fun getByRatingAndUser(rating: Rating, user: UserData) : Optional<Thumb>

    fun existsByUserAndRating(userData: UserData, get: Rating): Boolean
}
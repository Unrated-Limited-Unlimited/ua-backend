package com.ulu.repositories

import com.ulu.models.Rating
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface RatingRepository : JpaRepository<Rating,Long>{

}
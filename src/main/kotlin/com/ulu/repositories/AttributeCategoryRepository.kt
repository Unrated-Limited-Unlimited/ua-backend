package com.ulu.repositories

import com.ulu.models.AttributeCategory
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface AttributeCategoryRepository : JpaRepository<AttributeCategory,Long> {
    fun existsByName(name: String): Boolean

    @Query(value = "SELECT a.category FROM Attribute a WHERE a.rating.whiskey.id = :whiskeyId", nativeQuery = false)
    fun findByWhiskeyId(whiskeyId: Long): List<AttributeCategory>
}
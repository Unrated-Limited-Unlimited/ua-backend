package com.ulu.repositories

import com.ulu.models.AttributeCategory
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface AttributeCategoryRepository : JpaRepository<AttributeCategory,Long> {
    fun existsByName(name: String): Boolean
}
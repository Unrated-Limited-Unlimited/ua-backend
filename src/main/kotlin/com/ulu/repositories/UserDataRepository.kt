package com.ulu.repositories

import com.ulu.models.UserData
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable

@Repository
interface UserDataRepository : JpaRepository<UserData, Long> {
    fun existsByName(name: String): Boolean

    fun getUserDataByName(name: String): UserData?

    fun getByNameContainsIgnoreCase(
        name: String,
        pageable: Pageable,
    ): Page<UserData>
}

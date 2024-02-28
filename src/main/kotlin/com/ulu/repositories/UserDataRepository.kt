package com.ulu.repositories

import com.ulu.models.UserData
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface UserDataRepository : JpaRepository<UserData, Long> {

    fun getUserDataByName(name: String) : UserData
}

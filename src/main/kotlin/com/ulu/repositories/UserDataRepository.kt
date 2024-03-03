package com.ulu.repositories

import com.ulu.models.UserData
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface UserDataRepository : JpaRepository<UserData, Long> {

    fun existsByName(name: String) : Boolean
    fun getUserDataByName(name: String) : UserData
    fun getUserDataByNameAndPassword(identity: String, secret: String): UserData?
}

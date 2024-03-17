package com.ulu.repositories

import com.ulu.models.JwtRefreshToken
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface JwtRefreshTokenRepository : JpaRepository<JwtRefreshToken, Long> {
    fun getByRefreshToken(refreshToken: String) : JwtRefreshToken?

    fun updateRevokedByUsername(username: String, revoked: Boolean)
}
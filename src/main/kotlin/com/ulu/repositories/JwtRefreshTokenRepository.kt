package com.ulu.repositories

import com.ulu.models.RefreshToken
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface JwtRefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findByRefreshToken(refreshToken: String) : RefreshToken?

    fun updateRevokedByUsername(username: String, revoked: Boolean)
}
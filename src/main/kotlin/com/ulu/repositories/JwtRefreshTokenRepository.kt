package com.ulu.repositories

import com.ulu.models.JwtRefreshToken
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface JwtRefreshTokenRepository : JpaRepository<JwtRefreshToken, Long> {
    fun getByRefreshToken(refreshToken: String) : JwtRefreshToken?

    fun updateRevokedByUsername(username: String, revoked: Boolean)

    fun updateRevokedByRefreshToken(refreshToken: String, revoked: Boolean)

    fun getRevokedByRefreshToken(refreshToken: String) : Boolean

    // Find the JwtRefreshToken using one of the access token present in JwtRefreshToken.accessTokens
    @Query("SELECT j FROM JwtRefreshToken j WHERE :accessToken MEMBER OF j.accessTokens")
    fun findByAccessToken(accessToken: String): JwtRefreshToken?

    // Used to add a new access token to the JwtRefreshToken.accessTokens collection.
    @Query(value = "INSERT INTO jwt_refresh_token_access_tokens (jwt_refresh_token_id, access_tokens) VALUES (:id, :accessToken)", nativeQuery = true)
    fun addAccessTokenToJwtRefreshToken(id: Long, accessToken: String)
}
package com.ulu.models


import jakarta.persistence.*
import java.time.Instant

/**
 * A standalone JPA entity for keeping track of issued refresh tokens and access tokens.
 *
 * They will be marked invalid when users logs out or changes password
 * */
@Entity
data class JwtRefreshToken(
    @Id
    @GeneratedValue
    val id: Long? = null,

    var username: String,
    var refreshToken: String,
    var revoked: Boolean,

    var dateCreated: Instant? = Instant.now()
)

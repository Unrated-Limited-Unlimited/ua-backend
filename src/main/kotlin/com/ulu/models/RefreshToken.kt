package com.ulu.models


import jakarta.persistence.*
import java.time.Instant

@Entity
data class RefreshToken(
    @Id
    @GeneratedValue
    val id: Long? = null,

    var username: String,
    var refreshToken: String,

    var revoked: Boolean,

    var dateCreated: Instant? = null
)

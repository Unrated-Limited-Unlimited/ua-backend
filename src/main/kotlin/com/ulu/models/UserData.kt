package com.ulu.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.Instant

@Entity
class UserData(
    @Id @GeneratedValue
    val id: Long? = null,

    val name: String,
    val email: String,
    val password: String,
    val img: String,
    val createdAt: Instant = Instant.now()

    // Created At
)
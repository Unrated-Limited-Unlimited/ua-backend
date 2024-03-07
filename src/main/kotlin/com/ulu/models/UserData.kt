package com.ulu.models

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.Instant

@Entity
class UserData(
    @Id
    @GeneratedValue
    val id: Long? = null,

    var name: String,
    var email: String,
    var password: String,
    var img: String?,   //Store bytes?
    val createdAt: Instant = Instant.now(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    val ratings: MutableList<Rating> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    var thumbs: MutableList<Thumb> = mutableListOf()
)
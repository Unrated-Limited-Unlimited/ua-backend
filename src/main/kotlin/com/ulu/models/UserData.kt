package com.ulu.models

import jakarta.persistence.*
import java.time.Instant

@Entity
class UserData(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @Column(unique = true)
    var name: String,
    var email: String,
    var password: String,
    var img: String?,   //Store bytes?
    val createdAt: Instant = Instant.now(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    val ratings: MutableList<Rating> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    var thumbs: MutableList<Thumb> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    val roles : MutableList<String> = mutableListOf("ROLE_USER")
)
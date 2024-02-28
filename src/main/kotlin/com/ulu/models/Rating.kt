package com.ulu.models

import jakarta.persistence.*
import java.time.Instant


@Entity
class Rating (
    @Id
    @GeneratedValue
    val id: Long? = null,

    @ManyToOne
    var user : UserData? = null,

    @ManyToOne
    var whiskey: Whiskey? = null,

    val title: String,
    val body: String,
    val rating: Float,
    val createdAt: Instant = Instant.now(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rating", orphanRemoval = true)
    var thumbs: MutableList<Thumb> = mutableListOf()
)

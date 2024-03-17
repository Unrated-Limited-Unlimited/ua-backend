package com.ulu.models

import jakarta.persistence.*
import java.time.Instant


@Entity
class Rating(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @ManyToOne
    var user: UserData? = null,

    @ManyToOne
    var whiskey: Whiskey? = null,

    var title: String,
    var body: String,
    var score: Double,
    val createdAt: Instant = Instant.now(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rating", cascade = [CascadeType.ALL], orphanRemoval = true)
    var thumbs: MutableList<Thumb> = mutableListOf()
)

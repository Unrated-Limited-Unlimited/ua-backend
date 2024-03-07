package com.ulu.models

import jakarta.persistence.*

@Entity
class Whiskey(
    @Id
    @GeneratedValue
    val id: Long? = null,

    val img: String,
    val title: String, //name?
    val price: Float,
    val summary: String,
    val volume: Float,
    val percentage: Float,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "whiskey", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ratings: MutableList<Rating> = mutableListOf(),

    var rating: Float? = 0f,
)
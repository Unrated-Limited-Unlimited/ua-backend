package com.ulu.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Whiskey(
    @Id @GeneratedValue
    val id: Long? = null,

    val img: String,
    val title: String,
    val price: Float,
    val summary: String,
    val volume: Float,
    val percentage: Float,

    @OneToMany
    val ratings: List<Rating>? = null,

    var rating: Float? = 0f,
)
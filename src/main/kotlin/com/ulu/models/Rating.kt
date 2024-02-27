package com.ulu.models

import jakarta.persistence.*
import java.time.Instant


@Entity
class Rating (
    @Id
    @GeneratedValue
    val id: Long? = null,

    @ManyToOne
    val user : UserData,

    @ManyToOne
    val whiskey: Whiskey,

    val title: String,
    val body: String,
    val rating: Float,
    val createdAt: Instant = Instant.now(),

    //thumbs: List<Thumb>
)

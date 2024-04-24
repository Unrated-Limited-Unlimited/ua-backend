package com.ulu.models

import jakarta.persistence.*
import java.time.Instant
import kotlin.jvm.Transient

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
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rating", orphanRemoval = true)
    var thumbs: MutableList<Thumb> = mutableListOf(),
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rating", orphanRemoval = true)
    var attributes: MutableList<Attribute> = mutableListOf(),
    @Transient
    var votedThumb: Thumb? = null,
    @Transient
    var goodThumbs: Int = 0,
    @Transient
    var badThumbs: Int = 0,
)

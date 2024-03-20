package com.ulu.models

import jakarta.persistence.*

/**
 * Labels that can be added to reviews by users.
 * The labels are pre-defined by administrators and users can add a selection of them to their reviews.
 * It will also be used for fetching more personalized whiskeys based on earlier ratings.
 * */
@Entity
class Label(
    @Id
    @GeneratedValue
    val id : Long? = null,

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "labels")
    val ratings: MutableList<Rating> = mutableListOf(),

    var name: String,
)
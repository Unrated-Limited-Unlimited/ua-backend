package com.ulu.models

import jakarta.persistence.*

/**
 * Attributes that can be added to ratings by every user.
 * An attribute consist of a category along with a user defined score value.
 * The categories are pre-defined by administrators and users can add a selection of them, to their rating reviews.
 * It will also be used for fetching more personalized whiskeys based on earlier ratings.
 * */
@Entity
class Attribute(
    @Id
    @GeneratedValue
    val id : Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    val rating: Rating,

    @ManyToOne(fetch = FetchType.EAGER)
    val category: AttributeCategory,

    var score: Double,
)
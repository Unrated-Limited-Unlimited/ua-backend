package com.ulu.models

import jakarta.persistence.*

/**
 * Attributes that can be added to reviews by users.
 * */
@Entity
class AttributeCategory(
    @Id
    @GeneratedValue
    val id : Long? = null,

    var name: String,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "category", orphanRemoval = true)
    val attributes: MutableList<Attribute> = mutableListOf(),

    var avgScore: Double = 0.0,
)
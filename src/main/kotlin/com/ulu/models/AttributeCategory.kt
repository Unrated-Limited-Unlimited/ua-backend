package com.ulu.models

import jakarta.persistence.*

/**
 * Attributes that can be added to reviews by users.
 * */
@Entity
class AttributeCategory(
    @Id
    @GeneratedValue
    val id: Long? = null,

    var name: String,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "category", orphanRemoval = true)
    val attributes: MutableList<Attribute> = mutableListOf(),

    var avgScore: Double = 0.0,
) {
    fun calculateAvgScore() {
        var totalScore = 0.0
        var count = 0
        this.attributes.forEach { attribute: Attribute ->
            totalScore += attribute.score
            count++
        }
        avgScore = if (count > 0) {
            (totalScore / count)
        } else {
            0.0
        }
    }
}
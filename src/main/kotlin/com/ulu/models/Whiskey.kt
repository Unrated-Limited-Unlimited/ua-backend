package com.ulu.models

import jakarta.persistence.*
import kotlin.jvm.Transient

@Entity
class Whiskey(
    @Id @GeneratedValue val id: Long? = null,
    var img: String,
    var title: String, // name?
    var summary: String,
    var price: Double,
    var volume: Double,
    var percentage: Double,
    @OneToMany(
        fetch = FetchType.EAGER,
        mappedBy = "whiskey",
        orphanRemoval = true,
    ) var ratings: MutableList<Rating> = mutableListOf(),
    // Used when serializing to GraphQL
    @Transient
    var review: Rating? = null, // Will be populated if user has rated this whiskey.
    @Transient
    var categories: List<AttributeCategory> = mutableListOf(),
    var avgScore: Double = 0.0,
) {
    fun calculateAvgScore() {
        var totalScore = 0.0
        var count = 0
        this.ratings.forEach { rating: Rating ->
            totalScore += rating.score
            count++
        }
        avgScore =
            if (count > 0) {
                (totalScore / count)
            } else {
                0.0
            }
    }
}

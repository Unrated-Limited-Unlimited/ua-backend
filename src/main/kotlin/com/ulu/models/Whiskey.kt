package com.ulu.models

import jakarta.persistence.*

@Entity
class Whiskey(
    @Id @GeneratedValue val id: Long? = null,

    var img: String,
    var title: String, //name?
    var summary: String,

    var price: Double,
    var volume: Double,
    var percentage: Double,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "whiskey", cascade = [CascadeType.ALL],
        orphanRemoval = true
    ) var ratings: MutableList<Rating> = mutableListOf(),

    var avgScore: Double? = 0.0,
) {
    fun calculateAvgScore(): Double {
        var totalScore = 0.0
        var count = 0
        this.ratings.forEach { rating: Rating ->
            totalScore += rating.score
            count++
        }
        return if (count > 0) {
            (totalScore / count)
        } else {
            0.0
        }
    }
}


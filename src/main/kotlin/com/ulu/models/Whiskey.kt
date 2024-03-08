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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "whiskey", orphanRemoval = true)
    var ratings: MutableList<Rating> = mutableListOf(),

    var rating: Float? = 0f,
)
{
    fun calculateRating(): Float {
        var totalRating = 0f
        var count = 0
        this.ratings.forEach { rating: Rating ->
            totalRating += rating.rating
            count++
        }
        return if (count > 0) totalRating / count else 0f
    }
}


package com.ulu.models

import jakarta.persistence.*

@Entity
class Whiskey(
    @Id
    @GeneratedValue
    val id: Long? = null,

    var img: String,
    var title: String, //name?
    var price: Float,
    var summary: String,
    var volume: Float,
    var percentage: Float,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "whiskey", cascade = [CascadeType.ALL], orphanRemoval = true)
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


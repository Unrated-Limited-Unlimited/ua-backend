package com.ulu.models

import jakarta.persistence.*
import kotlin.jvm.Transient

@Entity
class Whiskey(
    @Id @GeneratedValue val id: Long? = null,

    var img: String,
    var title: String, //name?
    var summary: String,

    var price: Double,
    var volume: Double,
    var percentage: Double,

    @OneToMany(
        fetch = FetchType.EAGER, mappedBy = "whiskey",
        orphanRemoval = true
    ) var ratings: MutableList<Rating> = mutableListOf(),

    // Used when serializing to GraphQL
    @Transient
    var categories : List<AttributeCategory> = mutableListOf(),

    var avgScore: Double = 0.0,
) {
    fun calculateAvgScore() {
        var totalScore = 0.0
        var count = 0
        this.ratings.forEach { rating: Rating ->
            totalScore += rating.score
            count++
        }
        avgScore = if (count > 0) {
            (totalScore / count)
        } else {
            0.0
        }
    }

    /**
     * Calculate the average attribute scores given to the specific whiskey, for every category.
     * The result is stored temporarily in AttributeCategory.avgScore,
     * so it can be accessed and populated by the GraphQL requests.
     * */
    fun calculateAvgAttributeCategoryScore() {
        val flattenedAttributes = ratings.flatMap { it.attributes }
        val categoriesMap = mutableMapOf<String, AttributeCategory>()

        // Populate the categoriesMap with categories from flattenedAttributes
        for (attribute in flattenedAttributes) {
            val categoryName = attribute.category.name
            if (!categoriesMap.containsKey(categoryName)) {
                categoriesMap[categoryName] = attribute.category
            }
        }

        // Group attributes by category name and calculate average score
        val groupedAttributes = flattenedAttributes.groupBy { it.category.name }
        groupedAttributes.forEach { (categoryName, attributes) ->
            val avgScore = attributes.map { it.score }.average()
            categoriesMap[categoryName]?.avgScore = avgScore
        }
    }
}


package com.ulu.sorters

import com.ulu.models.Whiskey

/***
 * Sorts a list of whiskeys based on most positive ratings weighted up against amount of ratings descending.
 * Thus, whiskey with lower average score can be sorted higher if it has many reviews.
 */
class SortByBestRating {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        val whiskeyWithRatings = whiskeys.filter { it.ratings.isNotEmpty() } // Remove whiskeys with no ratings

        return whiskeyWithRatings.sortedByDescending { whiskey ->
            val averageRating = whiskey.calculateAvgScore()
            val numberOfRatings = whiskey.ratings.size
            averageRating * numberOfRatings // This is the weighting formula, might need tweaking
        }
    }
}

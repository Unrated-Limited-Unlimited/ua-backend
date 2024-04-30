package com.ulu.sorters

import com.ulu.models.Whiskey

/***
 * Sorts a list of whiskeys based on most positive ratings weighted up against amount of ratings descending.
 * Thus, whiskey with lower average score can be sorted higher if it has many reviews.
 */
class SortByHighestRating {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        val whiskeyWithRatings = whiskeys.filter { it.ratings.isNotEmpty() } // Remove whiskeys with no ratings

        return whiskeyWithRatings.sortedByDescending { whiskey ->
            whiskey.calculateAvgScore()
            whiskey.avgScore
        }
    }
}
package com.ulu.sorters

import com.ulu.models.Whiskey

/***
 * Sorts a list of whiskeys decending based on most ratings
 */
class SortByTotalRatings() {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.sortedByDescending { it.ratings.size }
    }
}
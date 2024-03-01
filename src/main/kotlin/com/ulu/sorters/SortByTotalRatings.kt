package com.ulu.sorters

import com.ulu.models.Whiskey

class SortByTotalRatings() {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.sortedByDescending { it.ratings.size }
    }
}
package com.ulu.sorters

import com.ulu.models.Whiskey

/***
 * Sorts a list of whiskeys by price per cl
 */
class SortByPrice {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.sortedByDescending { it.price / it.volume }
    }
}
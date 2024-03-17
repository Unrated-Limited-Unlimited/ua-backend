package com.ulu.sorters

import com.ulu.models.Whiskey

/***
 * Sorts a list of whiskeys randomly
 */
class SortByRandom() {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.shuffled()
    }
}
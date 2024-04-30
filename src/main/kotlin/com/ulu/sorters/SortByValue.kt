package com.ulu.sorters

import com.ulu.models.Whiskey

class SortByValue {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.sortedByDescending { it.price / it.volume }
    }
}
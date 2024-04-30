package com.ulu.sorters

import com.ulu.models.Whiskey

class SortByValue {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        val whiskeyWithValue = whiskeys.filter { it.volume != 0.0 }
        return whiskeyWithValue.sortedByDescending { it.price / it.volume }
    }
}
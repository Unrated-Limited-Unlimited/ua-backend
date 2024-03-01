package com.ulu.sorters

import com.ulu.models.Whiskey

class SortByRandom() {
    fun sortWhiskey(whiskeys : List<Whiskey>) :  List<Whiskey> {
        return whiskeys.shuffled()
    }
}
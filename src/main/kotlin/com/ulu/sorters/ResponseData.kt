package com.ulu.sorters

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ResponseData(
    val list: List<Int>
)
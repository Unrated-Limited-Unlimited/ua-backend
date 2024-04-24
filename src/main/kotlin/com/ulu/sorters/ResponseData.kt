package com.ulu.sorters

import kotlinx.serialization.Serializable

@Serializable
data class ResponseData(
    val list: List<Int>
)
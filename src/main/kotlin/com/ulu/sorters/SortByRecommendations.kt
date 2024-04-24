package com.ulu.sorters

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import java.net.URL
import kotlin.jvm.optionals.getOrElse

@Singleton
class SortByRecommendations(val whiskeyRepository: WhiskeyRepository) {

    fun sortWhiskey(whiskeys : List<Whiskey>, id : Long) :  List<Whiskey> {
        val url = "http://127.0.0.1:5000"
        val client = HttpClient.create(URL(url))

        //Create the POST request
        val requestBody = mapOf("number" to id)
        val request = HttpRequest.POST("/process", requestBody).contentType("application/json")

        val response = client.toBlocking().exchange(request, String::class.java)
        val responseData = Json.decodeFromString<ResponseData>(response.body())
        val whiskeyIds = responseData.list

        val returnList :  MutableList<Whiskey> = mutableListOf()
        whiskeyIds.forEach{
            returnList.add(whiskeyRepository.findById(it.toLong()).getOrElse { return@forEach })
        }
        return returnList
    }
}
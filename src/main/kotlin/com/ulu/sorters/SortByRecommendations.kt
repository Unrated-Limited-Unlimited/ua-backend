package com.ulu.sorters

import com.ulu.models.Whiskey
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Singleton

@Singleton
class SortByRecommendations {
    @field:Client("http://127.0.0.1:5000")
    lateinit var httpClient: HttpClient

    fun sortWhiskey(whiskeys : List<Whiskey>, id : Long) :  List<Whiskey> {
        //Create the POST request
        val request: HttpRequest<Any> = HttpRequest.POST("/process", "{$id}")

        val response = httpClient.toBlocking().exchange(request, String::class.java)

        println(response.body())

        return whiskeys.shuffled()
    }
}
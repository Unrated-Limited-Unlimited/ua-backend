package com.ulu.sorters

import com.ulu.models.Whiskey
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Singleton
import java.net.URL


@Singleton
class SortByRecommendations {

    fun sortWhiskey(whiskeys : List<Whiskey>, id : Long) :  List<Whiskey> {
        val url = "http://127.0.0.1:5000"
        val client = HttpClient.create(URL(url))

        //Create the POST request
        val request = HttpRequest.POST("/process", "{$id}")

        val response = client.toBlocking().exchange(request, String::class.java)

        println(response.body())

        return whiskeys.shuffled()
    }
}
package com.ulu.sorters

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrElse

@Singleton
class SortByRecommendations(val whiskeyRepository: WhiskeyRepository) {
    @Inject
    @field:Client("http://127.0.0.1:5000") // Base url for recommendation AI server.
    lateinit var client: HttpClient

    fun sortWhiskey(id: Long): List<Whiskey> {
        // Create the POST request
        val requestBody = mapOf("number" to id)
        val request = HttpRequest.POST("/process", requestBody).contentType("application/json")

        val responseMono = Mono.from(client.exchange(request, String::class.java))
        val response = responseMono.block() ?: error("Error communicating with recommendation service.")

        val responseData = Json.decodeFromString<ResponseData>(response.body())
        val whiskeyIds = responseData.list

        val returnList: MutableList<Whiskey> = mutableListOf()
        whiskeyIds.forEach {
            returnList.add(whiskeyRepository.findById(it.toLong()).getOrElse { return@forEach })
        }
        return returnList
    }
}

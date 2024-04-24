package com.ulu

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import com.ulu.sorters.SortByRecommendations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"])
class RecommendationTest(private val whiskeyRepository: WhiskeyRepository) {

    @Inject
    lateinit var sortByRecommendations: SortByRecommendations
    @Test
    fun runTest()
    {
        val whiskey : List<Whiskey>  = whiskeyRepository.findAll();
        sortByRecommendations.sortWhiskey(whiskey, 1)

    }
}
package com.ulu

import com.ulu.sorters.SortByRecommendations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"])
@Tag("manual")
class RecommendationTest {
    @Inject
    lateinit var sortByRecommendations: SortByRecommendations

    @Test
    fun runTest() {
        sortByRecommendations.sortWhiskey(1)
    }
}

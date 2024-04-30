package com.ulu

import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.services.AccountService
import com.ulu.sorters.SortByBestRating
import com.ulu.sorters.SortByPrice
import com.ulu.sorters.SortByTotalRatings
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["test"])
class SortersTest(private val accountService: AccountService) {
    private val whiskeyList: MutableList<Whiskey> = mutableListOf()

    private var whiskey1: Whiskey? = null
    private var whiskey2: Whiskey? = null
    private var whiskey3: Whiskey? = null

    @BeforeEach
    fun setup() {
        val user1 =
            UserData(
                name = "Mark",
                email = "mark@email.no",
                password = accountService.hashPassword("123"),
                img = "www.test.com/5.png",
            )

        // Add whiskey products
        whiskey1 =
            Whiskey(
                img = "test.com/img",
                title = "Test",
                price = 199.6,
                summary = "its a nice whiskey!",
                volume = 1.5,
                percentage = 99.9,
            )
        whiskey2 =
            Whiskey(
                img = "test2.com/img",
                title = "Test2",
                price = 15.0,
                summary = "it is another whiskey",
                volume = 0.4,
                percentage = 21.0,
            )
        whiskey3 =
            Whiskey(
                img = "test2.com/img",
                title = "Test2",
                price = 10.0,
                summary = "it is another whiskey",
                volume = 0.4,
                percentage = 21.0,
            )

        // Create ratings
        val rating1 = Rating(body = "test", score = 0.1, title = "its drinkable", user = user1, whiskey = whiskey1)
        val rating2 = Rating(body = "test", score = 0.2, title = "its not drinkable", user = user1, whiskey = whiskey2)
        val rating3 = Rating(body = "test", score = 0.8, title = "its amazing", user = user1, whiskey = whiskey2)

        whiskey1!!.ratings.add(rating1)
        whiskey2!!.ratings.add(rating2)
        whiskey2!!.ratings.add(rating3)

        whiskeyList.clear()
        whiskeyList.add(whiskey1!!)
        whiskeyList.add(whiskey2!!)
        whiskeyList.add(whiskey3!!)
    }

    @Test
    fun bestRatingTest() {
        val sorted = SortByBestRating().sortWhiskey(whiskeyList)

        assertEquals(sorted[0], whiskey2!!)
        assertEquals(sorted[1], whiskey1!!)

        // Whiskey 3 has no ratings.
        assertEquals(sorted.size, 2)
    }

    @Test
    fun byPriceTest() {
        val sorted = SortByPrice().sortWhiskey(whiskeyList)

        assertEquals(sorted[0], whiskey1!!)
        assertEquals(sorted[1], whiskey2!!)
        assertEquals(sorted[2], whiskey3!!)
    }

    @Test
    fun byTotalRating() {
        val sorted = SortByTotalRatings().sortWhiskey(whiskeyList)

        assertEquals(sorted[0], whiskey2!!)
        assertEquals(sorted[1], whiskey1!!)
        assertEquals(sorted[2], whiskey3!!)
    }

    @Test
    fun byHighestRating(){
        val sorted = SortByBestRating().sortWhiskey(whiskeyList)
        assertEquals(sorted[0], whiskey2!!)
        assertEquals(sorted[1], whiskey1!!)
    }
}

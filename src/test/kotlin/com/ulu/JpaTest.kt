package com.ulu


import com.ulu.models.*
import com.ulu.repositories.RatingRepository
import com.ulu.repositories.WhiskeyRepository
import com.ulu.services.AccountCreationService
import com.ulu.services.DatabaseService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Test class for testing JPA object functions/models/repositories.
 * */
@MicronautTest(environments = ["test"])
class JpaTest(private val databaseService: DatabaseService, private val whiskeyRepository: WhiskeyRepository, private val ratingRepository: RatingRepository) {
    @Test
    fun test() {
        val userData = UserData(name = "John", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        val whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9,
            price = 199.0,
            volume = 10.0
        )
        val rating =
            Rating(user = userData, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)
        val thumb = Thumb(user = userData, rating = rating, isGood = true)

        // Save
        databaseService.save(userData)
        databaseService.save(whiskey)
        databaseService.save(rating)
        databaseService.save(thumb)

        assertNotNull(userData.id)
        assertTrue(databaseService.exists(userData))
        assertTrue(databaseService.exists(whiskey))
        assertTrue(databaseService.exists(rating))
        assertTrue(databaseService.exists(thumb))

        // Delete
        databaseService.delete(userData)
        databaseService.delete(whiskey)
        databaseService.delete(rating)
        databaseService.delete(thumb)

        assertFalse(databaseService.exists(userData))
        assertFalse(databaseService.exists(whiskey))
        assertFalse(databaseService.exists(rating))
        assertFalse(databaseService.exists(thumb))
    }

    @Test
    fun removeChildTest(){
        val userData = UserData(name = "John", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        val whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9,
            price = 199.0,
            volume = 10.0
        )
        val rating =
            Rating(user = userData, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)

        // Save
        databaseService.save(userData)
        databaseService.save(whiskey)
        databaseService.save(rating)

        // Delete whiskey & rating
        whiskey.ratings.remove(rating)
        rating.whiskey = null
        whiskeyRepository.saveAndFlush(whiskey)
        ratingRepository.saveAndFlush(rating)

        whiskeyRepository.delete(whiskey)
        ratingRepository.delete(rating)

        assertFalse(whiskeyRepository.existsById(whiskey.id))
        assertFalse(ratingRepository.existsById(rating.id))
    }

    @Test
    fun avgWhiskeyScoreTest(){
        val whiskey = Whiskey(img = "img", title = "Test", price = 100.0, summary = "Whiskey", volume = 2.0, percentage = 50.0)
        val userData1 = UserData(name = "John", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        val userData2 = UserData(name = "John", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        val rating1 = Rating(user = userData1, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)
        val rating2 = Rating(user = userData2, whiskey = whiskey, title = "Good", body = "This is an in-depth review.", score = 5.0)

        // Simulate adding to JPA
        whiskey.ratings.add(rating1)
        whiskey.ratings.add(rating2)

        assertEquals(whiskey.calculateAvgScore(), 3.5)

        whiskey.ratings.clear()
        assertEquals(whiskey.calculateAvgScore(), 0.0)

        rating1.score = 0.0
        whiskey.ratings.add(rating1)
        assertEquals(whiskey.calculateAvgScore(), 0.0)
    }

    @Test
    fun avgWhiskeyAttributeCategoryScoreTest(){
        val attributeCategory1 = AttributeCategory(name = "Apocalypse Suitability")
        val attributeCategory2 = AttributeCategory(name = "Expandability Rate")

        val whiskey = Whiskey(img = "img", title = "Test", price = 100.0, summary = "Whiskey", volume = 2.0, percentage = 50.0)

        val userData1 = UserData(name = "John1", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        val userData2 = UserData(name = "John2", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")

        val rating1 = Rating(user = userData1, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)
        val rating2 = Rating(user = userData2, whiskey = whiskey, title = "Good", body = "This is an in-depth review.", score = 5.0)

        val attribute1 = Attribute(rating = rating1, category = attributeCategory1, score = 2.0)
        val attribute2 = Attribute(rating = rating1, category = attributeCategory2, score = 5.0)
        val attribute3 = Attribute(rating = rating2, category = attributeCategory1, score = 5.0)

        // Simulate adding to JPA
        rating1.attributes.add(attribute1)
        rating1.attributes.add(attribute2)
        rating2.attributes.add(attribute3)

        whiskey.ratings.add(rating1)
        whiskey.ratings.add(rating2)

        whiskey.calculateAvgAttributeCategoryScore()
        assertEquals(attributeCategory1.avgScore, 3.5)
        assertEquals(attributeCategory2.avgScore, 5.0)
    }
}
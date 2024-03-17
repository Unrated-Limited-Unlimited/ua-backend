package com.ulu


import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.repositories.RatingRepository
import com.ulu.repositories.WhiskeyRepository
import com.ulu.security.AccountCreationService
import com.ulu.services.DatabaseService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

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
}
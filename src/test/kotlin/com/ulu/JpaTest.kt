package com.ulu


import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.services.DatabaseService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["test"])
class JpaTest(private val databaseService: DatabaseService) {
    @Test
    fun test() {
        val userData = UserData(name = "John", password = "321", email = "test@proton.com", img = "img.txt")
        val whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9f,
            price = 199f,
            volume = 10f
        )
        val rating =
            Rating(user = userData, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", rating = 2f)
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
    fun removeOrphanTest(){
        val userData = UserData(name = "John", password = "321", email = "test@proton.com", img = "img.txt")
        val whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9f,
            price = 199f,
            volume = 10f
        )
        val rating =
            Rating(user = userData, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", rating = 2f)

        // Save
        databaseService.save(userData)
        databaseService.save(whiskey)
        databaseService.save(rating)

        databaseService.delete(whiskey)

        assertFalse(databaseService.exists(rating))
    }
}
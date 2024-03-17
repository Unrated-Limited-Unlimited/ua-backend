package com.ulu

import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.security.AccountCreationService
import com.ulu.services.DatabaseService
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.Micronaut.run
import io.micronaut.runtime.event.annotation.EventListener
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*
import jakarta.inject.Singleton

@OpenAPIDefinition(
    info = Info(
            title = "unrated",
            version = "0.0"
    )
)
object Api {
}
fun main(args: Array<String>) {
	run(*args)
}


@Singleton
class TestDataCreator(private val dbService: DatabaseService) {

    @EventListener
    fun onStartup(event: StartupEvent) {
        // Add test users
        val user1 = UserData(name = "Jeff", email = "jeff@bank.no", password = AccountCreationService().hashPassword("123"), img = "www.test.com/1.png")
        val user2 = UserData(name = "Paul", email = "pauling@gmail.com", password = AccountCreationService().hashPassword("42"), img = "www.test.com/2.png")

        // Add whiskey products
        val whiskey1 = Whiskey(img = "test.com/img", title = "Test", price = 199.6f, summary = "its a whiskey", volume = 1.5f, percentage = 99.9f)
        val whiskey2 = Whiskey(img = "test2.com/img", title = "Test2", price = 5f, summary = "it is another whiskey", volume = 0.4f, percentage = 21f)

        // Create ratings
        val rating1 = Rating(body = "test", rating = 3f, title = "its drinkable", user = user1, whiskey = whiskey1)
        val rating2 = Rating(body = "test", rating = 1f, title = "its not drinkable", user = user2, whiskey = whiskey2)
        val rating3 = Rating(body = "test", rating = 4f, title = "its amazing", user = user1, whiskey = whiskey2)

        // Like review
        val thumb1 = Thumb(user = user1, rating = rating1, isGood = true)

        dbService.save(user1)
        dbService.save(user2)

        dbService.save(whiskey1)
        dbService.save(whiskey2)

        dbService.save(rating1)
        dbService.save(rating2)
        dbService.save(rating3)

        dbService.save(thumb1)
    }
}
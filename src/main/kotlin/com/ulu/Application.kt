package com.ulu

import com.ulu.models.*
import com.ulu.repositories.UserDataRepository
import com.ulu.services.AccountCreationService
import com.ulu.services.DatabaseService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.Micronaut.run
import io.micronaut.runtime.event.annotation.EventListener
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*
import jakarta.inject.Singleton

@OpenAPIDefinition(
    info =
        Info(
            title = "unrated",
            version = "0.0",
        ),
)
object Api

fun main(args: Array<String>) {
    run(*args)
}

@Singleton
class TestDataCreator(private val dbService: DatabaseService, private val userDataRepository: UserDataRepository) {
    @Value("\${ADMIN_PASS:undefined}")
    lateinit var adminPass: String

    @EventListener
    @Requires(notEnv = ["prod"])
    fun onStartup(event: StartupEvent) {
        if (userDataRepository.getUserDataByName("Jeff") != null) {
            return
        }
        // Add some silly attribute test categories
        val attributeCategory1 = AttributeCategory(name = "Time Travel Capability")
        val attributeCategory2 = AttributeCategory(name = "Conversation Starter Level")

        // Add test users
        val user1 =
            UserData(
                name = "Jeff",
                email = "jeff@bank.no",
                password = AccountCreationService().hashPassword("123"),
                img = "www.test.com/1.png",
            )
        val user2 =
            UserData(
                name = "jeff",
                email = "john@gmail.com",
                password = AccountCreationService().hashPassword("42"),
                img = "www.test.com/2.png",
            )

        // Make user1 admin, so he can edit/create whiskeys/labels/ratings.
        user1.roles.add("ROLE_ADMIN")

        // Add whiskey products
        val whiskey1 =
            Whiskey(img = "test.com/img", title = "Test", price = 199.6, summary = "its a whiskey", volume = 1.5, percentage = 99.9)
        val whiskey2 =
            Whiskey(img = "test2.com/img", title = "Test2", price = 5.0, summary = "it is another whiskey", volume = 0.4, percentage = 21.0)

        // Create ratings
        val rating1 = Rating(body = "test", score = 0.1, title = "its drinkable", user = user1, whiskey = whiskey1)
        val rating2 = Rating(body = "test", score = 0.2, title = "its not drinkable", user = user2, whiskey = whiskey2)
        val rating3 = Rating(body = "test", score = 0.8, title = "its amazing", user = user1, whiskey = whiskey2)

        // Like review
        val thumb1 = Thumb(user = user1, rating = rating1, isGood = true)

        // Add attribute scores to review
        val attribute1 = Attribute(rating = rating2, category = attributeCategory1, score = 0.2)
        val attribute2 = Attribute(rating = rating2, category = attributeCategory2, score = 0.4)
        val attribute3 = Attribute(rating = rating3, category = attributeCategory2, score = 0.8)

        // Save created test objects
        dbService.save(attributeCategory1)
        dbService.save(attributeCategory2)

        dbService.save(user1)
        dbService.save(user2)

        dbService.save(whiskey1)
        dbService.save(whiskey2)

        dbService.save(rating1)
        dbService.save(rating2)
        dbService.save(rating3)

        dbService.save(thumb1)

        dbService.save(attribute1)
        dbService.save(attribute2)
        dbService.save(attribute3)
    }

    @EventListener
    @Requires(env = ["prod"])
    fun onProdStartup(event: StartupEvent) {
        if (userDataRepository.getUserDataByName("ADMIN") != null) {
            return
        }
        if (adminPass == "undefined") {
            error("Could not find environment variable: ADMIN_PASS")
        }
        val adminUser =
            UserData(name = "ADMIN", email = "admin@email.com", password = AccountCreationService().hashPassword(adminPass), img = "ADMIN")
        adminUser.roles.add("ROLE_ADMIN")
        dbService.save(adminUser)
    }
}

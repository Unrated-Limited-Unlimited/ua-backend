package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.AttributeCategory
import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.services.AccountCreationService
import com.ulu.services.DatabaseService
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["test"])
class RatingTest(@Client("/") private val client: HttpClient, private val databaseService: DatabaseService) {
    private var user: UserData? = null
    private var whiskey: Whiskey? = null
    private var rating: Rating? = null
    private var attributeCategory: AttributeCategory? = null

    @BeforeEach
    fun setup() {
        user = UserData(
            name = "John",
            password = AccountCreationService().hashPassword("321"),
            email = "test@proton.com",
            img = "img.txt"
        )
        whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9,
            price = 199.0,
            volume = 10.0
        )
        rating =
            Rating(user = user, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)

        attributeCategory = AttributeCategory(name = "Epic Level")

        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
        databaseService.save(attributeCategory)
    }

    @AfterEach
    fun cleanUp(){
        databaseService.deleteAll()
    }


    @Test
    fun getRatingTest() {
        val query =
            """ { "query": "{ getRating(id:\"${rating?.id}\") { id, title, score, body, whiskey{title} user{name} } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("getRating"))

        val ratingMap = map["getRating"] as Map<*, *>

        assertEquals(rating?.title, ratingMap["title"])
        assertEquals(rating?.score, ratingMap["score"])

        val whiskeyMap = ratingMap["whiskey"] as Map<*, *>
        assertEquals(whiskey?.title, whiskeyMap["title"])

        val userMap = ratingMap["user"] as Map<*, *>
        assertEquals(user?.name, userMap["name"])
    }

    @Test
    fun editRatingTest() {
        val query =
            """ { "query": "mutation{ editRating(id:\"${rating?.id}\", ratingInput: {title: \"New title\" }) { id, title, body, score } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("editRating"))

        val editRatingMap = map["editRating"] as Map<*, *>
        assertEquals("New title", editRatingMap["title"])
    }

    @Test
    fun createRatingTest() {
        val query =
            """ { "query": "mutation{ createRating(whiskeyId: \"${whiskey?.id}\", ratingInput: {title: \"New rating of whiskey!\", body: \"A whiskey rating\", score: 5 }) { id, title, body, score } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("createRating"))

        val createRatingMap = map["createRating"] as Map<*, *>
        assertEquals("New rating of whiskey!", createRatingMap["title"])
        assertEquals(5.0, createRatingMap["score"])
    }

    @Test
    fun createRatingWithAttributesTest() {
        val query =
            """ { "query": "mutation{ createRating(whiskeyId: \"${whiskey?.id}\", ratingInput: {title: \"New rating of whiskey!\", body: \"A whiskey rating\", score: 1.0 }, attributeInputs: [{id: ${attributeCategory?.id}, score: 0.4}]) { id, title, body, score, attributes{score} } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("createRating"))

        val createRatingMap = map["createRating"] as Map<*, *>
        assertEquals("New rating of whiskey!", createRatingMap["title"])
        assertEquals(1.0, createRatingMap["score"])
        assertEquals(0.4, ((createRatingMap["attributes"] as List<*>)[0] as Map<*,*>)["score"])
    }

    @Test
    fun deleteRatingTest() {
        val query = """ { "query": "mutation{ deleteRating(id: \"${rating?.id}\") }" } """
        val body = makeRequest(query)

        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("deleteRating"))
        assertEquals("deleted", map["deleteRating"])
    }

    private fun getJwtToken(): String {
        // Login
        val credentials = UsernamePasswordCredentials(user?.name, "321")
        val request: HttpRequest<*> = HttpRequest.POST("/login", credentials)
        val rsp: HttpResponse<BearerAccessRefreshToken> =
            client.toBlocking().exchange(request, BearerAccessRefreshToken::class.java)
        assertEquals(HttpStatus.OK, rsp.status)

        // Validate token
        val bearerAccessRefreshToken: BearerAccessRefreshToken = rsp.body()
        assertTrue(JWTParser.parse(bearerAccessRefreshToken.accessToken) is SignedJWT)

        // return JWT token as global var
        return bearerAccessRefreshToken.accessToken
    }

    private fun makeRequest(query: String): Map<String, Any> {
        val requestWithAuthorization = HttpRequest.POST("/graphql", query).bearerAuth(getJwtToken())
        val response = client.toBlocking().exchange(
            requestWithAuthorization, Argument.mapOf(
                String::class.java,
                Any::class.java
            )
        )
        assertEquals(HttpStatus.OK, response.status)
        println(response.body())
        return response.body()
    }
}

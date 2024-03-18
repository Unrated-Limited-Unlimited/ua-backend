package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.Rating
import com.ulu.models.Thumb
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.repositories.RatingRepository
import com.ulu.repositories.ThumbRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.repositories.WhiskeyRepository
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
class GraphQLThumbTest(
    @Client("/") private val client: HttpClient,
    private val databaseService: DatabaseService,
    private val userDataRepository: UserDataRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val ratingRepository: RatingRepository,
    private val thumbRepository: ThumbRepository
) {
    private var user: UserData? = null
    private var whiskey: Whiskey? = null
    private var rating: Rating? = null
    private var rating2: Rating? = null
    private var thumb: Thumb? = null

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

        rating2 =
            Rating(user = user, whiskey = whiskey, title = "Mid++", body = "This is an in-depth review.", score = 3.0)

        // Like own review rating
        thumb = Thumb(user = user, rating = rating, isGood = true)
        user?.thumbs?.add(thumb!!)

        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
        databaseService.save(rating2)
        databaseService.save(thumb)
    }

    @AfterEach
    fun cleanup() {
        thumbRepository.deleteAll()
        ratingRepository.deleteAll()

        whiskeyRepository.deleteAll()
        userDataRepository.deleteAll()
    }

    @Test
    fun getThumbTest() {
        val query =
            """ { "query": "{ getThumb(ratingId:\"${rating?.id}\") {  id, rating{title},user{name}, isGood  } }" }" """
        println(query)
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("getThumb"))

        val thumbMap = map["getThumb"] as Map<*, *>
        assertEquals(thumb?.isGood, thumbMap["isGood"])

        val whiskeyMap = thumbMap["rating"] as Map<*, *>
        assertEquals(rating?.title, whiskeyMap["title"])

        val userMap = thumbMap["user"] as Map<*, *>
        assertEquals(user?.name, userMap["name"])
    }

    @Test
    fun editThumbTest() {
        val query =
            """ { "query": "mutation{ editThumb(id:\"${thumb?.id}\", isGood: false) { id, rating{title},user{name}, isGood } }" }" """

        println(query)
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("editThumb"))

        val editThumbMap = map["editThumb"] as Map<*, *>
        assertEquals(false, editThumbMap["isGood"])
    }

    @Test
    fun createThumbTest() {
        val query =
            """ { "query": "mutation{ createThumb(ratingId: \"${rating2?.id}\", isGood: false) { id, rating{title},user{name}, isGood  } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("createThumb"))

        val editThumbMap = map["createThumb"] as Map<*, *>
        assertEquals(false, editThumbMap["isGood"])
    }

    @Test
    fun deleteThumbTest() {
        val query = """ { "query": "mutation{ deleteThumb(id: \"${thumb?.id}\") }" } """
        val body = makeRequest(query)

        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("deleteThumb"))
        assertEquals("deleted", map["deleteThumb"])

        assertFalse(databaseService.exists(thumb))
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
                String::class.java, Any::class.java
            )
        )
        assertEquals(HttpStatus.OK, response.status)
        println(response.body())
        return response.body()
    }
}

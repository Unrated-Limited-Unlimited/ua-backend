package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.security.AccountCreationService
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
class GraphQLWhiskeyTest(@Client("/") private val client: HttpClient, private val databaseService: DatabaseService) {
    private var user: UserData? = null
    private var whiskey: Whiskey? = null
    private var rating: Rating? = null

    @BeforeEach
    fun setup() {
        user = UserData(name = "John", password = AccountCreationService().hashPassword("321"), email = "test@proton.com", img = "img.txt")
        whiskey = Whiskey(
            title = "test",
            summary = "Its a test",
            img = "owl.png",
            percentage = 99.9f,
            price = 199f,
            volume = 10f
        )
        rating =
            Rating(user = user, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", rating = 2f)

        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
    }

    @AfterEach
    fun cleanup() {
        databaseService.delete(user)
        databaseService.delete(whiskey)
        databaseService.delete(rating)
    }

    @Test
    fun getWhiskeyTest() {
        val query =
            """ { "query": "{ getWhiskey(id:\"${whiskey?.id}\") { id, title, rating, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("getWhiskey"))

        val whiskeyById = whiskeyInfo["getWhiskey"] as Map<*, *>

        assertEquals(whiskey?.title, whiskeyById["title"])
        assertEquals(2.0, whiskeyById["rating"]) // Is calculated from request

        val ratings = whiskeyById["ratings"] as ArrayList<*>
        val ratingMap = ratings[0] as Map<*, *>
        val userMap = ratingMap["user"] as Map<*, *>
        assertNotNull(ratings)
        assertNotNull(userMap)

        assertEquals(rating?.body, ratingMap["body"])
        assertEquals(user?.name, userMap["name"])
    }

    @Test
    fun editWhiskeyTest() {
        val query =
            """ { "query": "mutation{ editWhiskey(id:\"${whiskey?.id}\", whiskey: {title: \"New title\" }) { id, title, summary, rating, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("editWhiskey"))
        assertTrue(whiskeyInfo.containsKey("New title"))
    }

    @Test
    fun createWhiskeyTest() {
        val query =
            """ { "query": "mutation{ createWhiskey(whiskey: {title: \"New Whiskey\", summary: \"A whiskey\", img: \"whiskey.png\", price: 199.9, volume: 10.0, percentage: 10.0 }) { id, title, summary, rating, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("createWhiskey"))
        assertTrue(whiskeyInfo.containsKey("New Whiskey"))
    }

    @Test
    fun deleteWhiskeyTest() {
        val query = """ { "query": "mutation{ deleteWhiskey(id: \"${whiskey?.id}\") }" } """
        val body = makeRequest(query)

        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("deleteWhiskey"))
        assertEquals("ok", whiskeyInfo["deleteWhiskey"])

        assertFalse(databaseService.exists(whiskey))
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

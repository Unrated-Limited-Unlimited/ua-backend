package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
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
    private var user : UserData? = null
    private var whiskey : Whiskey? = null
    private var rating : Rating? = null

    @BeforeEach
    fun setup(){
        user = UserData(name = "John", password = "321", email = "test@proton.com", img = "img.txt")
        whiskey = Whiskey(title = "test", summary = "Its a test", img = "owl.png", percentage = 99.9f, price = 199f, volume = 10f)
        rating = Rating(user= user, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", rating = 2f)

        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
    }

    @AfterEach
    fun cleanup(){
        databaseService.delete(user)
        databaseService.delete(whiskey)
        databaseService.delete(rating)
    }

    @Test
    fun getWhiskeyTest() {
        val body = whiskey?.id?.let {
            makeRequest("\"\"\"{ \"query\": \"{ getWhiskey(id:\\\"$it\\\") { id, title, rating, ratings { user{name}, body } } }\" }\"\"\"")
        }
        assertNotNull(body)

        val whiskeyInfo = body?.get("data") as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("getWhiskey"))

        val whiskeyById = whiskeyInfo["getWhiskey"] as Map<*, *>

        assertEquals(whiskey?.title, whiskeyById["title"])
        assertEquals(2.0, whiskeyById["rating"]) // Is calculated from request

        val ratings = whiskeyById["ratings"] as ArrayList<*>
        val ratingMap = ratings[0] as Map<*,*>
        val userMap = ratingMap["user"] as Map<*, *>
        assertNotNull(ratings)
        assertNotNull(userMap)

        assertEquals(rating?.body, ratingMap["body"])
        assertEquals(user?.name, userMap["name"])
    }

    @Test
    fun editWhiskeyTest(){
        val body = whiskey?.id?.let {
            makeRequest("\"\"\"{ \"query\": \"{ editWhiskey(id:\\\"$it\\\", whiskey{}) }\" }\"\"\"")
        }
        assertNotNull(body)

        val whiskeyInfo = body?.get("data") as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("deleteWhiskey"))
        assertTrue(whiskeyInfo.containsKey("ok"))
    }

    @Test
    fun createWhiskeyTest(){
        val body = whiskey?.id?.let {
            makeRequest("\"\"\"{ \"query\": \"{ createWhiskey(id: \\\"$it\\\") }\" }\"\"\"")
        }
        assertNotNull(body)

        val whiskeyInfo = body?.get("data") as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("createWhiskey"))
        assertTrue(whiskeyInfo.containsKey("ok"))
    }

    @Test
    fun deleteWhiskeyTest(){
        val body = whiskey?.id?.let {
            makeRequest("\"\"\"{ \"query\": \"{ deleteWhiskey(id:\\\"$it\\\") }\" }\"\"\"")
        }
        assertNotNull(body)

        val whiskeyInfo = body?.get("data") as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("deleteWhiskey"))
        assertTrue(whiskeyInfo.containsKey("ok"))
    }

    private fun getJwtToken() : String{
        // Login
        val credentials = UsernamePasswordCredentials(user?.name, user?.password)
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
        println(query)
        val request: HttpRequest<String> = HttpRequest.POST("/graphql", query).bearerAuth(getJwtToken())
        val rsp = client.toBlocking().exchange(
            request, Argument.mapOf(
                String::class.java,
                Any::class.java
            )
        )
        assertEquals(HttpStatus.OK, rsp.status())
        println(rsp.body())
        return rsp.body()
    }
}

package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
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
class GraphQLUserDataTest(@Client("/") private val client: HttpClient, private val databaseService: DatabaseService) {
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
            percentage = 99.9,
            price = 199.0,
            volume = 10.0
        )
        rating =
            Rating(user = user, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)

        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
    }

    @AfterEach
    fun cleanup() {
        databaseService.deleteAll()
    }

    @Test
    fun getLoggedInUserTest() {
        val query = """ { "query": "{ getLoggedInUser { id, name, img, ratings { whiskey{title}, body } } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val userInfo = body["data"] as Map<*, *>
        println(userInfo.toString())
        assertTrue(userInfo.containsKey("getLoggedInUser"))

        val loggedInUserMap = userInfo["getLoggedInUser"] as Map<*, *>

        assertEquals(user?.name, loggedInUserMap["name"])
    }

    @Test
    fun getUserTest() {
        val query =
            """ { "query": "{ getUser(name:\"${user?.name}\") { id, name, img, ratings { whiskey{title}, body } } }" }" """
        val body = makeRequest(query)

        assertNotNull(body)

        val userInfo = body["data"] as Map<*, *>
        println(userInfo.toString())
        assertTrue(userInfo.containsKey("getUser"))

        val getUserMap = userInfo["getUser"] as Map<*, *>

        assertEquals(user?.name, getUserMap["name"])
    }

    @Test
    fun editUserTest() {
        val query =
            """ { "query": "mutation{ editUser(user: {email: \"new@email.com\" } ) { id, name, email, img, ratings { whiskey{title}, body } } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val userInfo = body["data"] as Map<*, *>
        println(userInfo.toString())
        assertTrue(userInfo.containsKey("editUser"))

        val editUserMap = userInfo["editUser"] as Map<*, *>
        assertEquals("new@email.com", editUserMap["email"])

        // Check that unspecified params are not changed to null
        assertEquals(user?.img, editUserMap["img"])
        assertNotNull(editUserMap["img"])
    }

    @Test
    fun deleteUserTest() {
        val query = """ { "query": "mutation{ deleteUser }" } """
        val body = makeRequest(query)
        assertNotNull(body)

        val deleteUserInfo = body["data"] as Map<*, *>
        println(deleteUserInfo.toString())
        assertTrue(deleteUserInfo.containsKey("deleteUser"))

        assertEquals("deleted", deleteUserInfo["deleteUser"])
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

package com.ulu.security

import com.ulu.repositories.UserDataRepository
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["test"])
class AuthControllerTest(
    @Client("/") private val client: HttpClient,
    private val userDataRepository: UserDataRepository
) {
    data class RegisterDTO(
        val username: String,
        val password: String,
        val email: String? = null,
        val img: String? = null
    )

    @Test
    fun testAccountController() {
        // Register a new account
        val credentials = RegisterDTO("NewUser", "superSafePassword1881", "test@mail.com", "test.png")
        val request: HttpRequest<*> = HttpRequest.POST("/register", credentials)
        val response: HttpResponse<String> = client.toBlocking().exchange(request, String::class.java)
        assertEquals(HttpStatus.CREATED, response.status)

        assertNotNull(userDataRepository.getUserDataByName("NewUser"))

        // Login to get access token
        val loginCredentials = RegisterDTO("NewUser", "superSafePassword1881")
        val loginRequest: HttpRequest<*> = HttpRequest.POST("/login", loginCredentials)
        val loginResponse: HttpResponse<BearerAccessRefreshToken> =
            client.toBlocking().exchange(loginRequest, BearerAccessRefreshToken::class.java)
        assertEquals(HttpStatus.OK, loginResponse.status)
        assertNotNull(loginResponse.body().accessToken)

        // Verify access to /graphql using bearer token
        val graphQlRequest = HttpRequest.POST("/graphql", """ { "query": "{ getLoggedInUser{ id, name } }" }" """).bearerAuth(loginResponse.body().accessToken)
        val graphQlResponse = client.toBlocking().exchange(
            graphQlRequest, Argument.mapOf(
                String::class.java,
                Any::class.java
            )
        )
        assertEquals(HttpStatus.OK, graphQlResponse.status)
        assertNotNull(graphQlResponse.body())

        val userInfo = graphQlResponse.body()["data"] as Map<*, *>
        println(userInfo.toString())
        assertTrue(userInfo.containsKey("getLoggedInUser"))

        // Logout to revoke access_token
        val logoutRequest: HttpRequest<*> = HttpRequest.POST("/logout", "").bearerAuth(loginResponse.body().accessToken)
        val logoutResponse: HttpResponse<String> =
            client.toBlocking().exchange(logoutRequest, String::class.java)
        assertEquals(HttpStatus.OK, logoutResponse.status)

        // Verify revoked access
        try {
            val graphQlUnauthorizedRequest = HttpRequest.POST("/graphql", """ { "query": "{ getLoggedInUser{ id, name } }" }" """).bearerAuth(loginResponse.body().accessToken)
            val graphQlUnauthorizedResponse = client.toBlocking().exchange(
                graphQlUnauthorizedRequest, Argument.mapOf(
                    String::class.java,
                    Any::class.java
                )
            )
            assertEquals(HttpStatus.OK, graphQlUnauthorizedResponse.status)
            assertNotNull(graphQlUnauthorizedResponse.body())
        }
        catch (err : HttpClientResponseException){
            assertEquals(HttpStatus.UNAUTHORIZED, err.status)
        }
    }


    @Test
    fun badRegistrationRequestTest() {
        val credentials = RegisterDTO("Le Fishe", "notSafe", "t@m.com", "test")
        val request: HttpRequest<*> = HttpRequest.POST("/register", credentials)

        try {
            val response: HttpResponse<String> = client.toBlocking().exchange(request, String::class.java)
            assertNotEquals(HttpStatus.OK, response.status)
        } catch (err: HttpClientResponseException) {
            assertEquals(HttpStatus.BAD_REQUEST, err.status)

            val errorBody = err.response.getBody(String::class.java)
            errorBody.ifPresent { body ->
                println("Error response body: $body")
            }
        }
    }

    @Test
    fun emailValidatorTest() {
        assertTrue(AccountCreationService().isValidEmail("test@gmail.com"))
        assertTrue(AccountCreationService().isValidEmail("student.given.mail@institusion.edu"))

        assertFalse(AccountCreationService().isValidEmail("test@test"))
        assertFalse(AccountCreationService().isValidEmail("test@.no"))
        assertFalse(AccountCreationService().isValidEmail("@test.no"))
        assertFalse(AccountCreationService().isValidEmail("test"))
        assertFalse(AccountCreationService().isValidEmail(""))
    }
}

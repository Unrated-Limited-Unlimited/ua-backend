package com.ulu.security

import com.ulu.repositories.UserDataRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@MicronautTest
class AuthControllerTest(@Client("/") private val client: HttpClient, private val userDataRepository: UserDataRepository) {
    data class RegisterDTO(
        val username: String,
        val password: String,
        val email: String,
        val img: String?
    )

    @Test
    fun registerTest() {
        val credentials = RegisterDTO("NewUser","superSafePassword1881","test@mail.com","test.png")
        val request: HttpRequest<*> = HttpRequest.POST("/register", credentials)
        val rsp: HttpResponse<String> = client.toBlocking().exchange(request, String::class.java)
        assertEquals(HttpStatus.OK, rsp.status)

        assertNotNull(userDataRepository.getUserDataByName("NewUser"))
    }

    @Test
    fun badRequestTest(){
        val credentials = RegisterDTO("Le Fishe","notSafe","t@m.com","test")
        val request: HttpRequest<*> = HttpRequest.POST("/register", credentials)

        try {
            val response : HttpResponse<String> = client.toBlocking().exchange(request, String::class.java)
            assertNotEquals(HttpStatus.OK,response.status)
        } catch (e: HttpClientResponseException) {
            assertEquals(HttpStatus.BAD_REQUEST, e.status)

            val errorBody = e.response.getBody(String::class.java)
            errorBody.ifPresent { body ->
                println("Error response body: $body")
            }
        }
    }

    @Test
    fun emailValidatorTest(){
        assertTrue(AccountCreationService().isValidEmail("test@gmail.com"))
        assertTrue(AccountCreationService().isValidEmail("student.given.mail@institusion.edu"))

        assertFalse(AccountCreationService().isValidEmail("test@test"))
        assertFalse(AccountCreationService().isValidEmail("test@.no"))
        assertFalse(AccountCreationService().isValidEmail("@test.no"))
        assertFalse(AccountCreationService().isValidEmail("test"))
        assertFalse(AccountCreationService().isValidEmail(""))
    }
}
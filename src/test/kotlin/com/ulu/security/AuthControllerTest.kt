package com.ulu.security

import com.ulu.repositories.UserDataRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["test"])
class AuthControllerTest(@Client("/") private val client: HttpClient, private val userDataRepository: UserDataRepository) {
    data class RegisterDTO(
        val username: String,
        val password: String,
        val email: String,
        val img: String?
    )

    @Test
    fun registerTest() {
        val credentials = RegisterDTO("Le Fishe","15","t@m.com","test")
        val request: HttpRequest<*> = HttpRequest.POST("/register", credentials)
        val rsp: HttpResponse<String> = client.toBlocking().exchange(request, String::class.java)
        assertEquals(HttpStatus.OK, rsp.status)

        //TODO test more when reqs are decided.

        assertNotNull(userDataRepository.getUserDataByName("Le Fishe"))
    }
}
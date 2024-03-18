package com.ulu.security

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.services.AccountCreationService
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.MediaType.TEXT_PLAIN
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.endpoints.TokenRefreshRequest
import io.micronaut.security.token.render.AccessRefreshToken
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["test"])
class JwtAuthenticationTest(@Client("/") val client: HttpClient, private val userDataRepository: UserDataRepository, private val jwtRefreshTokenRepository: JwtRefreshTokenRepository) {
    private var userData = UserData(name = "Test", password = AccountCreationService().hashPassword("123"), email = "test@email.com", img = "www.test.com/images")

    @BeforeEach
    fun setup(){
        userData = UserData(name = "Test", password = AccountCreationService().hashPassword("123"), email = "test@email.com", img = "www.test.com/images")
        userDataRepository.save(userData)
    }

    @AfterEach
    fun cleanup(){
        userDataRepository.delete(userData)
    }

    @Test
    fun securedEndpointWithNoAuthReturnsUnauthorized() {
        val e = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange<Any, Any>(HttpRequest.GET<Any>("/").accept(TEXT_PLAIN))
        }
        assertEquals(UNAUTHORIZED, e.status)
    }

    @Test
    fun successfulLoginReturnsJWT() {
        // Login
        val credentials = UsernamePasswordCredentials(userData.name, "123")
        val request: HttpRequest<*> = HttpRequest.POST("/login", credentials)
        val rsp: HttpResponse<BearerAccessRefreshToken> =
            client.toBlocking().exchange(request, BearerAccessRefreshToken::class.java)
        assertEquals(OK, rsp.status)

        // Validate token
        val bearerAccessRefreshToken: BearerAccessRefreshToken = rsp.body()
        assertEquals(userData.name, bearerAccessRefreshToken.username)
        assertNotNull(bearerAccessRefreshToken.accessToken)
        assertNotNull(bearerAccessRefreshToken.refreshToken)
        assertTrue(JWTParser.parse(bearerAccessRefreshToken.accessToken) is SignedJWT)

        // Do a secured post graphql request using bearer token
        val getUserQuery = """{"query" :"{ getUser(name: \"${userData.name}\"){ id,name } }"}"""
        val accessToken: String = bearerAccessRefreshToken.accessToken

        val requestWithAuthorization = HttpRequest.POST("/graphql",getUserQuery).bearerAuth(accessToken)
        val response = client.toBlocking().exchange(
            requestWithAuthorization, Argument.mapOf(
                String::class.java,
                Any::class.java
            )
        )
        assertEquals(OK, rsp.status)
        assertEquals("{data={getUser={id=${userData.id}, name=${userData.name}}}}", response.body().toString())
    }

    @Test
    fun jwtRefresh(){
        val oldTokenCount = jwtRefreshTokenRepository.count()

        // Login
        val credentials = UsernamePasswordCredentials(userData.name, "123")
        val request: HttpRequest<*> = HttpRequest.POST("/login", credentials)
        val rsp: HttpResponse<BearerAccessRefreshToken> =
            client.toBlocking().exchange(request, BearerAccessRefreshToken::class.java)
        assertEquals(OK, rsp.status)

        Thread.sleep(3000)
        assertEquals(oldTokenCount + 1, jwtRefreshTokenRepository.count())

        val bearerAccessRefreshToken: BearerAccessRefreshToken = rsp.body()
        assertNotNull(bearerAccessRefreshToken.accessToken)
        assertNotNull(bearerAccessRefreshToken.refreshToken)

        Thread.sleep(1000) // sleep for one second to give time for the issued at `iat` Claim to change
        val refreshResponse = client.toBlocking().retrieve(HttpRequest.POST("/oauth/access_token",
            TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, bearerAccessRefreshToken.refreshToken)
        ), AccessRefreshToken::class.java)

        assertNotNull(refreshResponse.accessToken)
        assertNotEquals(bearerAccessRefreshToken.accessToken, refreshResponse.accessToken)

        jwtRefreshTokenRepository.deleteAll()
    }

}

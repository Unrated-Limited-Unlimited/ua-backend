package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.AttributeCategory
import com.ulu.models.Rating
import com.ulu.models.UserData
import com.ulu.models.Whiskey
import com.ulu.services.AccountService
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
class AttributeCategoryTest(
    @Client("/") private val client: HttpClient,
    private val databaseService: DatabaseService,
    private val accountService: AccountService,
) {
    private var user: UserData? = null
    private var whiskey: Whiskey? = null
    private var rating: Rating? = null
    private var attributeCategory: AttributeCategory? = null

    @BeforeEach
    fun setup() {
        attributeCategory = AttributeCategory(name = "Very Helpful Attribute")

        user =
            UserData(
                name = "John",
                password = accountService.hashPassword("321"),
                email = "test@proton.com",
                img = "img.txt",
            )
        user?.roles?.add("ROLE_ADMIN")

        whiskey =
            Whiskey(
                title = "test",
                summary = "Its a test",
                img = "owl.png",
                percentage = 99.9,
                price = 199.0,
                volume = 10.0,
            )
        rating =
            Rating(user = user, whiskey = whiskey, title = "Mid", body = "This is an in-depth review.", score = 2.0)

        databaseService.save(attributeCategory)
        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
    }

    @AfterEach
    fun cleanUp() {
        databaseService.deleteAll()
    }

    @Test
    fun getAttributeCategoryTest() {
        val query =
            """ { "query": "{ getAttributeCategories { id, name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        println(body["data"])

        val ratingMap = body["data"] as Map<*, *>
        assertTrue(ratingMap.containsKey("getAttributeCategories"))

        val ratingList = ratingMap["getAttributeCategories"] as List<*>
        println(ratingList)
        val rating = ratingList[ratingList.size - 1] as Map<*, *>
        assertEquals(attributeCategory?.name, rating["name"])
        assertNotNull(rating["id"])
    }

    @Test
    fun editAttributeCategoryTest() {
        val query =
            """ { "query": "mutation{ editAttributeCategory(id:\"${attributeCategory?.id}\", name: \"New AttributeCategory name\") { id,name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("editAttributeCategory"))

        val editRatingMap = map["editAttributeCategory"] as Map<*, *>
        assertEquals("New AttributeCategory name", editRatingMap["name"])
    }

    @Test
    fun createAttributeCategoryTest() {
        val query =
            """ { "query": "mutation{ createAttributeCategory(name: \"New AttributeCategory created!\") { id, name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("createAttributeCategory"))

        val createRatingMap = map["createAttributeCategory"] as Map<*, *>
        assertEquals("New AttributeCategory created!", createRatingMap["name"])
        assertNotNull(createRatingMap["id"])
    }

    @Test
    fun deleteAttributeCategoryTest() {
        val query = """ { "query": "mutation{ deleteAttributeCategory(id: \"${attributeCategory?.id}\") }" } """
        val body = makeRequest(query)

        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("deleteAttributeCategory"))
        assertEquals("deleted", map["deleteAttributeCategory"])
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
        val response =
            client.toBlocking().exchange(
                requestWithAuthorization,
                Argument.mapOf(
                    String::class.java,
                    Any::class.java,
                ),
            )
        assertEquals(HttpStatus.OK, response.status)
        println(response.body())
        return response.body()
    }
}

package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.Label
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
class LabelTest(@Client("/") private val client: HttpClient, private val databaseService: DatabaseService) {
    private var user: UserData? = null
    private var whiskey: Whiskey? = null
    private var rating: Rating? = null
    private var label: Label? = null

    @BeforeEach
    fun setup() {
        label = Label(name = "Very Helpful Label")

        user = UserData(
            name = "John",
            password = AccountCreationService().hashPassword("321"),
            email = "test@proton.com",
            img = "img.txt",
        )
        user?.roles?.add("ROLE_ADMIN")

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

        databaseService.save(label)
        databaseService.save(user)
        databaseService.save(whiskey)
        databaseService.save(rating)
    }

    @AfterEach
    fun cleanUp(){
        databaseService.deleteAll()
    }


    @Test
    fun getLabelTest() {
        val query =
            """ { "query": "{ getLabels { id, name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        println(body["data"])

        val ratingMap = body["data"] as Map<*,*>
        assertTrue(ratingMap.containsKey("getLabels"))

        val ratingList = ratingMap["getLabels"] as List<*>
        println(ratingList)
        val rating = ratingList[ratingList.size-1] as Map<*, *>
        assertEquals(label?.name, rating["name"])
        assertNotNull(rating["id"])
    }

    @Test
    fun editLabelTest() {
        val query =
            """ { "query": "mutation{ editLabel(id:\"${label?.id}\", name: \"New label name\") { id,name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("editLabel"))

        val editRatingMap = map["editLabel"] as Map<*, *>
        assertEquals("New label name", editRatingMap["name"])
    }

    @Test
    fun createRatingTest() {
        val query =
            """ { "query": "mutation{ createLabel(name: \"New label created!\") { id, name } }" }" """
        val body = makeRequest(query)
        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("createLabel"))

        val createRatingMap = map["createLabel"] as Map<*, *>
        assertEquals("New label created!", createRatingMap["name"])
        assertNotNull(createRatingMap["id"])
    }

    @Test
    fun deleteRatingTest() {
        val query = """ { "query": "mutation{ deleteLabel(id: \"${label?.id}\") }" } """
        val body = makeRequest(query)

        assertNotNull(body)

        val map = body["data"] as Map<*, *>
        println(map.toString())
        assertTrue(map.containsKey("deleteLabel"))
        assertEquals("deleted", map["deleteLabel"])
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

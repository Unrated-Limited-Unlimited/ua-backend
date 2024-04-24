package com.ulu.graphql

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.ulu.models.*
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
class WhiskeyTest(
    @Client("/") private val client: HttpClient,
    private val databaseService: DatabaseService,
    private val accountService: AccountService,
) {
    private var adminUser: UserData? = null
    private var user: UserData? = null
    private var whiskey1: Whiskey? = null
    private var whiskey2: Whiskey? = null
    private var whiskey3: Whiskey? = null
    private var whiskey4: Whiskey? = null
    private var rating1: Rating? = null
    private var rating2: Rating? = null
    private var rating3: Rating? = null
    private var attributeCategory: AttributeCategory? = null
    private var attribute: Attribute? = null

    @BeforeEach
    fun setup() {
        user =
            UserData(
                name = "Petra",
                password = accountService.hashPassword("111"),
                email = "testing@proton.com",
                img = "img.txt",
            )
        adminUser =
            UserData(
                name = "John",
                password = accountService.hashPassword("111"),
                email = "test@proton.com",
                img = "img.txt",
            )
        adminUser?.roles?.add("ROLE_ADMIN")

        whiskey1 =
            Whiskey(
                title = "test",
                summary = "Its a test",
                img = "owl.png",
                percentage = 99.9,
                price = 199.0,
                volume = 10.0,
            )

        whiskey2 =
            Whiskey(
                title = "test2",
                summary = "Its a test",
                img = "owl.png",
                percentage = 99.9,
                price = 99.0,
                volume = 10.0,
            )

        whiskey3 =
            Whiskey(
                title = "test3",
                summary = "Its a test",
                img = "owl.png",
                percentage = 99.9,
                price = 50.0,
                volume = 10.0,
            )

        whiskey4 =
            Whiskey(
                title = "banana",
                summary = "Its a test",
                img = "owl.png",
                percentage = 99.9,
                price = 99.0,
                volume = 10.0,
            )
        rating1 =
            Rating(user = adminUser, whiskey = whiskey1, title = "Mid", body = "This is an in-depth review.", score = 0.2)

        rating2 =
            Rating(user = adminUser, whiskey = whiskey3, title = "Mid", body = "This is an in-depth review.", score = 0.2)

        rating3 =
            Rating(user = adminUser, whiskey = whiskey2, title = "Bad", body = "This is an in-depth review.", score = 0.1)

        attributeCategory = AttributeCategory(name = "No Taste - Giga Taste")
        attribute = Attribute(category = attributeCategory!!, rating = rating1!!, score = 0.6)

        databaseService.save(adminUser)
        databaseService.save(user)
        databaseService.save(whiskey1)
        databaseService.save(whiskey2)
        databaseService.save(whiskey3)
        databaseService.save(whiskey4)
        databaseService.save(rating1)
        databaseService.save(rating2)
        databaseService.save(rating3)
        databaseService.save(attributeCategory)
        databaseService.save(attribute)
    }

    @AfterEach
    fun cleanup() {
        databaseService.deleteAll()
    }

    @Test
    fun getWhiskeyTest() {
        val query =
            """ { "query": "{ getWhiskey(id:\"${whiskey1?.id}\") { id, title, avgScore, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query, adminUser!!)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("getWhiskey"))

        val whiskeyById = whiskeyInfo["getWhiskey"] as Map<*, *>

        assertEquals(whiskey1?.title, whiskeyById["title"])
        assertEquals(0.2, whiskeyById["avgScore"]) // Is calculated from request

        val ratings = whiskeyById["ratings"] as ArrayList<*>
        val ratingMap = ratings[0] as Map<*, *>
        val userMap = ratingMap["user"] as Map<*, *>
        assertNotNull(ratings)
        assertNotNull(userMap)

        assertEquals(rating1?.body, ratingMap["body"])
        assertEquals(adminUser?.name, userMap["name"])
    }

    @Test
    fun getWhiskeysFilteredSortedTest() {
        println(attribute!!.id)
        val query =
            """ { "query": "{ getWhiskeys(sort: {sortType: PRICE, reverse: false}, filters: [ {comp: GT, field: { attribute: { id: ${attributeCategory!!.id}, avgScore: 0.5} }}, {field: { title: \"est\" }} ]) { id, title, avgScore, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query, adminUser!!)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("getWhiskeys"))

        val whiskeys = whiskeyInfo["getWhiskeys"] as List<*>

        assertEquals(1, whiskeys.size)

        val whiskeyById = whiskeys[0] as Map<*, *>

        assertEquals(whiskey1?.title, whiskeyById["title"])
        assertEquals(0.2, whiskeyById["avgScore"]) // Is calculated from request

        val ratings = whiskeyById["ratings"] as ArrayList<*>
        val ratingMap = ratings[0] as Map<*, *>
        val userMap = ratingMap["user"] as Map<*, *>
        assertNotNull(ratings)
        assertNotNull(userMap)

        assertEquals(rating1?.body, ratingMap["body"])
        assertEquals(adminUser?.name, userMap["name"])
    }

    @Test
    fun editWhiskeyTest() {
        val query =
            """ { "query": "mutation{ editWhiskey(id:\"${whiskey1?.id}\", whiskeyInput: {title: \"New title\" }) { id, title, summary, avgScore, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query, adminUser!!)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("editWhiskey"))
        assertTrue((whiskeyInfo["editWhiskey"] as Map<*, *>).containsValue("New title"))
    }

    @Test
    fun createWhiskeyTest() {
        val query =
            """ { "query": "mutation{ createWhiskey(whiskeyInput: {title: \"New Whiskey\", summary: \"A whiskey\", img: \"whiskey.png\", price: 199.9, volume: 10.0, percentage: 10.0 }) { id, title, summary, avgScore, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query, adminUser!!)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("createWhiskey"))

        assertTrue((whiskeyInfo["createWhiskey"] as Map<*, *>).containsValue("New Whiskey"))
    }

    @Test
    fun nonAdminDeniedCreateWhiskeyTest() {
        val query =
            """ { "query": "mutation{ createWhiskey(whiskeyInput: {title: \"New Whiskey\", summary: \"A whiskey\", img: \"whiskey.png\", price: 199.9, volume: 10.0, percentage: 10.0 }) { id, title, summary, avgScore, ratings { user{name}, body } } }" }" """
        val body = makeRequest(query, user!!)
        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("createWhiskey"))
        assertTrue(whiskeyInfo.containsValue(null))

        assertTrue(body.containsKey("errors"))
        val errorInfo = body["errors"] as List<*>
        assertTrue(errorInfo.isNotEmpty())
    }

    @Test
    fun deleteWhiskeyTest() {
        val query = """ { "query": "mutation{ deleteWhiskey(id: \"${whiskey1?.id}\") }" } """
        val body = makeRequest(query, adminUser!!)

        assertNotNull(body)

        val whiskeyInfo = body["data"] as Map<*, *>
        println(whiskeyInfo.toString())
        assertTrue(whiskeyInfo.containsKey("deleteWhiskey"))
        assertEquals("deleted", whiskeyInfo["deleteWhiskey"])
    }

    private fun getJwtToken(user: UserData): String {
        // Login
        val credentials = UsernamePasswordCredentials(user.name, "111")
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

    private fun makeRequest(
        query: String,
        user: UserData,
    ): Map<String, Any> {
        val requestWithAuthorization = HttpRequest.POST("/graphql", query).bearerAuth(getJwtToken(user))
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

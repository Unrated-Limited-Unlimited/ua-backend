package com.ulu.fetchers

import com.ulu.models.AttributeCategory
import com.ulu.models.Whiskey
import com.ulu.repositories.AttributeCategoryRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.repositories.WhiskeyRepository
import com.ulu.services.RequestValidatorService
import com.ulu.sorters.*
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class WhiskeyFetcher(
    private val whiskeyRepository: WhiskeyRepository,
    private val attributeCategoryRepository: AttributeCategoryRepository,
    private val securityService: DefaultSecurityService,
    private val userDataRepository: UserDataRepository,
) {
    fun getWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeyId: String =
                dataFetchingEnvironment?.getArgument("id")
                    ?: error("AttributeCategory with identical name already exists")
            val whiskey: Whiskey = whiskeyRepository.getWhiskeyById(whiskeyId.toLong())

            // Update the average rating scores and attribute scores for the whiskey
            whiskey.calculateAvgScore()
            whiskey.categories = attributeCategoryRepository.findByWhiskeyId(whiskey.id!!)
            whiskey.categories.map { a: AttributeCategory -> a.calculateAvgScore() }
            whiskey.calculateAvgAttributeCategoryScore()

            return@DataFetcher whiskey
        }
    }

    fun getWhiskeys(): DataFetcher<List<Whiskey>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeys = whiskeyRepository.findAll()

            // Update the average rating scores and attribute scores for the whiskey
            whiskeys.forEach {
                it.calculateAvgScore()

                it.categories = attributeCategoryRepository.findByWhiskeyId(it.id!!)
                it.categories.map { a: AttributeCategory -> a.calculateAvgScore() }

                it.calculateAvgAttributeCategoryScore()
            }

            val sortedWhiskey =
                dataFetchingEnvironment?.getArgument<String>("sortType").let {
                    when (it) {
                        "BEST" -> SortByBestRating().sortWhiskey(whiskeys)
                        "PRICE" -> SortByPrice().sortWhiskey(whiskeys)
                        "POPULAR" -> SortByTotalRatings().sortWhiskey(whiskeys)
                        "RANDOM" -> SortByRandom().sortWhiskey(whiskeys)
                        "Recommended" -> SortByRecommendations(whiskeyRepository).sortWhiskey(whiskeys, userDataRepository.getUserDataByName(securityService.authentication.get().name)!!.id ?: error("lmao, no ID found"))
                        else -> whiskeys
                    }
                }
            return@DataFetcher sortedWhiskey
        }
    }

    fun createWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            RequestValidatorService().verifyAdmin(securityService)

            val whiskeyInput =
                environment.getArgument("whiskeyInput") as Map<*, *>? ?: error("whiskey input not provided")
            return@DataFetcher whiskeyRepository.save(
                Whiskey(
                    title = whiskeyInput["title"] as String,
                    img = whiskeyInput["img"] as String,
                    summary = whiskeyInput["summary"] as String,
                    percentage = whiskeyInput["percentage"] as Double,
                    price = whiskeyInput["price"] as Double,
                    volume = whiskeyInput["volume"] as Double,
                ),
            )
        }
    }

    fun editWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            RequestValidatorService().verifyAdmin(securityService)

            val whiskey = getWhiskeyByEnvironmentId(environment)
            val whiskeyInput =
                environment.getArgument("whiskeyInput") as Map<*, *>? ?: error("whiskey input not provided")
            with(whiskey) {
                title = whiskeyInput["title"] as? String ?: title
                img = whiskeyInput["img"] as? String ?: img
                summary = whiskeyInput["summary"] as? String ?: summary

                percentage = whiskeyInput["percentage"] as? Double ?: percentage
                price = whiskeyInput["price"] as? Double ?: price
                volume = whiskeyInput["volume"] as? Double ?: volume
            }

            return@DataFetcher whiskeyRepository.update(whiskey)
        }
    }

    fun deleteWhiskey(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            RequestValidatorService().verifyAdmin(securityService)

            val whiskey = getWhiskeyByEnvironmentId(environment)
            whiskey.ratings.clear()
            whiskeyRepository.delete(whiskey)
            return@DataFetcher "deleted"
        }
    }

    private fun getWhiskeyByEnvironmentId(environment: DataFetchingEnvironment): Whiskey {
        val whiskeyId = (environment.getArgument("id") as String).toLong()
        val whiskey = whiskeyRepository.findById(whiskeyId)
        if (whiskey.isEmpty) {
            error("No whiskey with id $whiskeyId.")
        }
        return whiskey.get()
    }
}

package com.ulu.fetchers

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import com.ulu.sorters.SortByBestRating
import com.ulu.sorters.SortByPrice
import com.ulu.sorters.SortByRandom
import com.ulu.sorters.SortByTotalRatings
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService

import jakarta.inject.Singleton

@Singleton
class WhiskeyFetcher(
    private val whiskeyRepository: WhiskeyRepository,
    private val securityService: DefaultSecurityService
) {
    fun getWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeyId: String? = dataFetchingEnvironment?.getArgument("id")
            if (whiskeyId != null) {
                val whiskey: Whiskey = whiskeyRepository.getWhiskeyById(whiskeyId.toLong())
                whiskey.avgScore = whiskey.calculateAvgScore()
                return@DataFetcher whiskey
            } else {
                return@DataFetcher null
            }
        }
    }

    fun getWhiskeys(): DataFetcher<List<Whiskey>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeys = whiskeyRepository.findAll()
            val sortedWhiskey = dataFetchingEnvironment?.getArgument<String>("sortType").let {
                when (it) {
                    "BEST" -> SortByBestRating().sortWhiskey(whiskeys)
                    "PRICE" -> SortByPrice().sortWhiskey(whiskeys)
                    "POPULAR" -> SortByTotalRatings().sortWhiskey(whiskeys)
                    "RANDOM" -> SortByRandom().sortWhiskey(whiskeys)
                    else -> whiskeys
                }
            }
            return@DataFetcher sortedWhiskey
        }
    }

    fun createWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")){
                error("You must be an admin to edit whiskeys")
            }
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
                )
            )
        }
    }

    fun editWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")){
                error("You must be an admin to create new whiskeys")
            }
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
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")){
                error("You must be an admin to delete whiskeys")
            }
            val whiskey = getWhiskeyByEnvironmentId(environment)
            whiskey.ratings.clear()
            whiskeyRepository.delete(whiskey)
            return@DataFetcher "deleted"
        }
    }

    private fun getWhiskeyByEnvironmentId(environment : DataFetchingEnvironment) : Whiskey {
        val whiskeyId = (environment.getArgument("id") as String).toLong()
        val whiskey = whiskeyRepository.findById(whiskeyId)
        if (whiskey.isEmpty) {
            error("No whiskey with id $whiskeyId.")
        }
        return whiskey.get()
    }
}
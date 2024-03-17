package com.ulu.fetchers

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService

import jakarta.inject.Singleton

@Singleton
class WhiskeyFetcher(
    private val whiskeyRepository: WhiskeyRepository,
    private val securityService: DefaultSecurityService
) {
    private enum class SortType {
        RATING,
        POPULAR,
        //PERSONALIZED | based on users reviews on similar whiskeys
    }

    fun getWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeyId: String? = dataFetchingEnvironment?.getArgument("id")

            if (whiskeyId != null) {
                val whiskey: Whiskey = whiskeyRepository.getWhiskeyById(whiskeyId.toLong())
                whiskey.rating = whiskey.calculateRating()
                return@DataFetcher whiskey
            } else {
                return@DataFetcher null
            }
        }
    }

    fun getWhiskeys(): DataFetcher<List<Whiskey>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val sortType: SortType = dataFetchingEnvironment?.getArgument<String>("sortType").let {
                when (it) {
                    "RATING" -> SortType.RATING

                    else -> SortType.POPULAR
                }
            }

            //TODO Fetch whiskeys based on sortType enum.

            val whiskeys: List<Whiskey> = whiskeyRepository.findAll()
            whiskeys.forEach { whiskey: Whiskey ->
                whiskey.rating = whiskey.calculateRating()
            }
            return@DataFetcher whiskeys
        }
    }

    fun createWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            //if (!securityService.authentication.get().roles.contains("admin")){
            //    error("You must be an admin to create new whiskeys")
            //}
            val whiskeyInput =
                environment.getArgument("whiskeyInput") as Map<*, *>? ?: error("whiskey input not provided")
            return@DataFetcher whiskeyRepository.save(
                Whiskey(
                    title = whiskeyInput["title"] as String,
                    img = whiskeyInput["img"] as String,
                    percentage = (whiskeyInput["percentage"] as Double).toFloat(),
                    price = (whiskeyInput["price"] as Double).toFloat(),
                    summary = whiskeyInput["summary"] as String,
                    volume = (whiskeyInput["volume"] as Double).toFloat()
                )
            )
        }
    }

    fun editWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskey = getOwnedWhiskeyById(environment)
            val whiskeyInput =
                environment.getArgument("whiskeyInput") as Map<*, *>? ?: error("whiskey input not provided")

            (whiskeyInput["title"] as? String)?.let { nonNullTitle ->
                whiskey.title = nonNullTitle
            }
            (whiskeyInput["img"] as? String)?.let { nonNullImg ->
                whiskey.img = nonNullImg
            }
            (whiskeyInput["summary"] as? String)?.let { nonNullSummary ->
                whiskey.summary = nonNullSummary
            }

            (whiskeyInput["percentage"] as? Double)?.let { nonNullPercentage ->
                whiskey.percentage = nonNullPercentage.toFloat()
            }
            (whiskeyInput["price"] as? Double)?.let { nonNullPrice ->
                whiskey.price = nonNullPrice.toFloat()
            }
            (whiskeyInput["volume"] as? Double)?.let { nonNullVolume ->
                whiskey.volume = nonNullVolume.toFloat()
            }
            return@DataFetcher whiskeyRepository.update(whiskey)
        }
    }

    fun deleteWhiskey(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskey = getOwnedWhiskeyById(environment)
            whiskey.ratings.clear()
            whiskeyRepository.delete(whiskey)
            return@DataFetcher "deleted"
        }
    }

    private fun getOwnedWhiskeyById(environment : DataFetchingEnvironment) : Whiskey {
        if (!securityService.isAuthenticated) {
            error("Unauthenticated")
        }
        val whiskeyId = (environment.getArgument("id") as String).toLong()
        val whiskey = whiskeyRepository.findById(whiskeyId)
        if (whiskey.isEmpty) {
            error("No whiskey with id $whiskeyId.")
        }
        return whiskey.get()
    }
}
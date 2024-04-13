package com.ulu.fetchers

import com.ulu.models.AttributeCategory
import com.ulu.models.Whiskey
import com.ulu.repositories.AttributeCategoryRepository
import com.ulu.repositories.WhiskeyRepository
import com.ulu.services.RequestValidatorService
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
    private val attributeCategoryRepository: AttributeCategoryRepository,
    private val securityService: DefaultSecurityService,
) {
    fun getWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskeyId: String =
                environment.getArgument("id")
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
        return DataFetcher { environment: DataFetchingEnvironment ->
            // Find whiskeys using paging
            var whiskeys: List<Whiskey> =
                whiskeyRepository.listAll(RequestValidatorService().getPaging(environment)).content

            // Update the average rating scores and attribute scores for the whiskey
            whiskeys.forEach {
                it.calculateAvgScore()
                it.categories = attributeCategoryRepository.findByWhiskeyId(it.id!!)
                it.categories.map { a: AttributeCategory -> a.calculateAvgScore() }
                it.calculateAvgAttributeCategoryScore()
            }

            // Filter whiskeys based on filters input
            whiskeys = filterWhiskeysByComparator(whiskeys, environment)

            // Sort whiskeys
            val sort = environment.getArgument("sort") as Map<*, *>
            val sortedWhiskey =
                sort["sortType"].let {
                    when (it) {
                        "BEST" -> SortByBestRating().sortWhiskey(whiskeys)
                        "PRICE" -> SortByPrice().sortWhiskey(whiskeys)
                        "POPULAR" -> SortByTotalRatings().sortWhiskey(whiskeys)
                        "RANDOM" -> SortByRandom().sortWhiskey(whiskeys)
                        else -> whiskeys
                    }
                }

            // Reverse sorted list
            val isReversed = sort["reverse"] as Boolean?
            if (isReversed != null && isReversed) {
                return@DataFetcher sortedWhiskey.reversed()
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

    /**
     * Use the given Comparator given in input to filter whiskey list.
     * */
    private fun filterWhiskeysByComparator(
        whiskeys: List<Whiskey>,
        environment: DataFetchingEnvironment,
    ): List<Whiskey> {
        val filters = environment.getArgument("filters") as List<Map<*, *>>? ?: return whiskeys

        var filteredWhiskeys = whiskeys
        filters.forEach { filter ->
            val field = filter["field"] as Map<*, *>

            // Filter by whiskey name, does not require comparator
            field["title"]?.let { title ->
                filteredWhiskeys =
                    filteredWhiskeys.filter { whiskey: Whiskey ->
                        whiskey.title.contains(title as String, ignoreCase = true)
                    }
                return@forEach
            }

            // Pattern match by Comparator enum
            filteredWhiskeys =
                when (filter["comp"] as String?) {
                    "LT" -> filterWhiskeys(whiskeys, field) { x, y -> x < y }
                    "GT" -> filterWhiskeys(whiskeys, field) { x, y -> x > y }
                    "LE" -> filterWhiskeys(whiskeys, field) { x, y -> x <= y }
                    "GE" -> filterWhiskeys(whiskeys, field) { x, y -> x >= y }
                    "EQ" -> filterWhiskeys(whiskeys, field) { x, y -> x == y }
                    else -> filteredWhiskeys
                }
        }
        return filteredWhiskeys
    }

    /**
     * Filter the given list of whiskeys by using the first non-null field variable given in input/environment
     * */
    private fun filterWhiskeys(
        whiskeys: List<Whiskey>,
        field: Map<*, *>,
        comparator: (Double, Double) -> Boolean,
    ): List<Whiskey> {
        // Filter by whiskey average score
        field["avgScore"]?.let { avgScore ->
            return whiskeys.filter { whiskey -> comparator(whiskey.avgScore, avgScore as Double) }
        }

        // Filter by attribute using attribute category id and avgScore
        field["attribute"]?.let { attribute ->
            attribute as Map<*, *>
            val attributeId = attribute["id"] as Int
            val attributeAvgScore = attribute["avgScore"] as Double

            return whiskeys.filter { whiskey ->
                whiskey.categories.any { category ->
                    (category.id == attributeId.toLong()) && (comparator(category.avgScore, attributeAvgScore))
                }
            }
        }

        // Unfiltered
        return whiskeys
    }
}

package com.ulu.fetchers

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

import jakarta.inject.Singleton

@Singleton
class WhiskeyFetcher(private val whiskeyRepository: WhiskeyRepository) {
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
            // TODO
            return@DataFetcher null
        }
    }

    fun editWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }

    fun deleteWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }
}
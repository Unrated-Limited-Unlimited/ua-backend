package com.ulu.fetchers

import com.ulu.models.Whiskey
import com.ulu.repositories.WhiskeyRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

import jakarta.inject.Singleton


@Singleton
class WhiskeyFetcher(private val whiskeyRepository: WhiskeyRepository) {

    private class WhiskeyInput(
        val title: String,
        val summary: String,
        val img: String,

        val price: Float,
        val volume: Float,
        val percentage: Float
    )
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
            val whiskeyInput = environment.getArgument("whiskeyInput") as WhiskeyInput
            return@DataFetcher null
        }
    }

    fun editWhiskey(): DataFetcher<Whiskey> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskeyId = (environment.getArgument("id") as String).toLong()
            val whiskey = whiskeyRepository.findById(whiskeyId)
            if (whiskey.isEmpty) {
                error("No whiskey with id $whiskeyId.")
            }
            val whiskeyInput = environment.getArgument("whiskeyInput") as WhiskeyInput
            whiskey.get().title = whiskeyInput.title
            whiskey.get().summary = whiskeyInput.summary
            whiskey.get().img = whiskeyInput.img
            whiskey.get().price = whiskeyInput.price
            whiskey.get().volume = whiskeyInput.volume
            whiskey.get().percentage = whiskeyInput.percentage

            return@DataFetcher whiskeyRepository.update(whiskey.get())
        }
    }

    fun deleteWhiskey(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskeyId = (environment.getArgument("id") as String).toLong()
            val whiskey = whiskeyRepository.findById(whiskeyId)
            if (whiskey.isEmpty) {
                error("No whiskey with id $whiskeyId.")
            }
            whiskeyRepository.deleteById(whiskeyId)
            return@DataFetcher "Deleted"
        }
    }
}
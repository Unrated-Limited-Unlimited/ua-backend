package com.ulu.fetchers

import com.ulu.models.Rating
import com.ulu.repositories.RatingRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.repositories.WhiskeyRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class RatingFetcher(
    private val ratingRepository: RatingRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val userDataRepository: UserDataRepository,
    private val securityService: DefaultSecurityService,
) {

    private class RatingInput(
        val title: String,
        val body: String,
        val rating: Float,
    )

    fun getRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("ratingId") as String).toLong()
            val rating = ratingRepository.findById(ratingId)
            if (rating.isEmpty) {
                error("Rating with id $ratingId not found")
            }
            return@DataFetcher rating.get()
        }
    }

    fun createRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val whiskeyId = (environment.getArgument("whiskeyId") as String).toLong()
            val whiskey = whiskeyRepository.findById(whiskeyId)
            if (whiskey.isEmpty) {
                error("No whiskey with id $whiskeyId.")
            }
            val ratingInput = environment.getArgument("ratingInput") as RatingInput
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)

            return@DataFetcher ratingRepository.save(Rating(user = userData, whiskey = whiskey.get(), body = ratingInput.body, rating = ratingInput.rating, title = ratingInput.title))
        }
    }

    fun editRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("id") as String).toLong()
            val rating = ratingRepository.findById(ratingId)
            if (rating.isEmpty) {
                error("No rating with id $ratingId.")
            }
            if (rating.get().user!!.name != securityService.authentication.get().name){
                error("Can't edit someone else's rating")
            }
            val ratingInput = environment.getArgument("ratingInput") as RatingInput
            rating.get().title = ratingInput.title
            rating.get().body = ratingInput.body
            rating.get().rating = ratingInput.rating
            return@DataFetcher ratingRepository.update(rating.get())
        }
    }

    fun deleteRating(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("id") as String).toLong()
            val rating = ratingRepository.findById(ratingId)
            if (rating.isEmpty) {
                error("No rating with id $ratingId.")
            }
            if (rating.get().user!!.name != securityService.authentication.get().name){
                error("Can't delete someone else's rating")
            }
            ratingRepository.delete(rating.get())
            return@DataFetcher "deleted"
        }
    }

}
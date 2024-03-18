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
    fun getRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("id") as String).toLong()
            val rating = ratingRepository.findById(ratingId)
            if (rating.isEmpty) {
                error("Rating with id $ratingId not found")
            }
            return@DataFetcher rating.get()
        }
    }

    fun createRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            if (!securityService.isAuthenticated){
                error("Unauthenticated")
            }
            val whiskeyId = (environment.getArgument("whiskeyId") as String).toLong()
            val whiskey = whiskeyRepository.findById(whiskeyId)
            if (whiskey.isEmpty) {
                error("No whiskey with id $whiskeyId.")
            }
            val ratingInput = environment.getArgument("ratingInput") as Map<*, *>
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)

            return@DataFetcher ratingRepository.save(
                Rating(
                    user = userData,
                    whiskey = whiskey.get(),
                    body = ratingInput["body"] as String,
                    score = ratingInput["score"] as Double,
                    title = ratingInput["title"] as String
                )
            )
        }
    }

    fun editRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val rating = getOwnedRatingById(environment)
            val ratingInput = environment.getArgument("ratingInput") as Map<*, *>
            with(rating){
                title = ratingInput["title"] as? String ?: title
                body = ratingInput["body"] as? String ?: body
                score = ratingInput["score"] as? Double ?: score
            }
            return@DataFetcher ratingRepository.update(rating)
        }
    }

    fun deleteRating(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val rating = getOwnedRatingById(environment)
            ratingRepository.delete(rating)
            return@DataFetcher "deleted"
        }
    }

    fun getOwnedRatingById(environment: DataFetchingEnvironment, argumentName: String = "id") : Rating {
        if (!securityService.isAuthenticated){
            error("Unauthenticated")
        }
        val ratingId = (environment.getArgument(argumentName) as String).toLong()
        val rating = ratingRepository.findById(ratingId)
        if (rating.isEmpty) {
            error("No rating with id $ratingId.")
        }
        val auth = securityService.authentication.get()
        if (rating.get().user?.name != auth.name && !auth.roles.contains("ROLE_ADMIN")) {
            error("Can't change someone else's rating")
        }
        return rating.get()
    }

}
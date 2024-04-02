package com.ulu.fetchers

import com.ulu.models.Thumb
import com.ulu.repositories.ThumbRepository
import com.ulu.repositories.UserDataRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class ThumbFetcher(
    private val securityService: DefaultSecurityService,
    private val thumbRepository: ThumbRepository,
    private val userDataRepository: UserDataRepository,
    private val ratingFetcher: RatingFetcher
) {

    fun getThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val rating = ratingFetcher.getOwnedRatingById(environment,"ratingId")
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)
                ?: error("No user found")
            val thumb = thumbRepository.getByRatingAndUser(rating = rating, user = userData)
            if (thumb.isEmpty) {
                error("Could not find thumb by ${userData.name} for rating with id: ${rating.id}")
            }
            return@DataFetcher thumb.get()
        }
    }

    fun createThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val isGood = environment.getArgument("isGood") as Boolean
            val rating = ratingFetcher.getOwnedRatingById(environment,"ratingId")
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)
                ?: error("User not found")
            if (thumbRepository.existsByUserAndRating(userData, rating)) {
                error("Thumb rating already exists for rating with id: ${rating.id}")
            }
            return@DataFetcher thumbRepository.save(Thumb(user = userData, rating = rating, isGood = isGood))

        }
    }

    fun editThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val thumb = getOwnedThumbById(environment)
            val isGood = environment.getArgument("isGood") as Boolean
            thumb.isGood = isGood
            return@DataFetcher thumbRepository.update(thumb)
        }
    }

    fun deleteThumb(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val thumb = getOwnedThumbById(environment)
            thumbRepository.delete(thumb)
            return@DataFetcher "deleted"
        }
    }

    private fun getOwnedThumbById(environment: DataFetchingEnvironment) : Thumb {
        if (!securityService.isAuthenticated){
            error("Unauthenticated")
        }
        val thumbId = (environment.getArgument("id") as String).toLong()
        val thumb = thumbRepository.findById(thumbId)
        if (thumb.isEmpty) {
            error("No thumb rating with id: $thumbId")
        }
        val auth = securityService.authentication.get()
        if (thumb.get().user?.name != auth.name){
            error("Can't change someone else's thumb review.")
        }
        return thumb.get()
    }
}
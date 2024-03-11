package com.ulu.fetchers

import com.ulu.models.Thumb
import com.ulu.repositories.RatingRepository
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
    private val ratingRepository: RatingRepository
) {

    fun getThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("ratingId") as String).toLong()
            val rating = ratingRepository.findById(ratingId)
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name) ?: error("No user found")
            if (rating.isEmpty){
                error("No rating with id $")
            }
            val thumb = thumbRepository.getByRatingAndUser(rating = rating.get(), user = userData)
            if (thumb.isEmpty){
                error("Could not find thumb by ${userData.name} for rating with id: $ratingId")
            }
            return@DataFetcher thumb.get()
        }
    }

    fun createThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val ratingId = (environment.getArgument("ratingId") as String).toLong()
            val isGood = environment.getArgument("isGood") as Boolean
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)
            val rating = ratingRepository.findById(ratingId)

            if (userData == null) {
                error("User not found")
            }
            if (rating.isEmpty){
                error("No rating with id: $ratingId")
            }
            if (thumbRepository.existsByUserAndRating(userData, rating.get())){
                error("Thumb rating already exists for rating with id: $ratingId")
            }
            return@DataFetcher thumbRepository.save(Thumb(user = userData, rating = rating.get(), isGood = isGood))

        }
    }

    fun editThumb(): DataFetcher<Thumb> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val thumbId = (environment.getArgument("id") as String).toLong()
            val isGood = environment.getArgument("isGood") as Boolean

            val thumb = thumbRepository.findById(thumbId)
            if (thumb.isEmpty){
                error("No thumb rating with id: $thumbId")
            }
            thumb.get().isGood = isGood
            return@DataFetcher thumbRepository.update(thumb.get())
        }
    }

    fun deleteThumb() : DataFetcher<String> {
        return DataFetcher{ environment: DataFetchingEnvironment ->
            val thumbId = (environment.getArgument("id") as String).toLong()

            if (thumbRepository.findById(thumbId).isEmpty){
                return@DataFetcher "No thumb rating with id: $thumbId"
            }
            thumbRepository.deleteById(thumbId)
            return@DataFetcher "ok"
        }
    }
}
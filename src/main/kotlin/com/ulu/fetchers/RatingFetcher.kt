package com.ulu.fetchers

import com.ulu.models.Attribute
import com.ulu.models.Rating
import com.ulu.repositories.*
import com.ulu.services.RequestValidatorService
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class RatingFetcher(
    private val ratingRepository: RatingRepository,
    private val whiskeyRepository: WhiskeyRepository,
    private val userDataRepository: UserDataRepository,
    private val attributeRepository: AttributeRepository,
    private val attributeCategoryRepository: AttributeCategoryRepository,
    private val securityService: DefaultSecurityService,
    private val requestValidatorService: RequestValidatorService,
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
            requestValidatorService.verifyAuthenticated(securityService)

            val whiskeyId = (environment.getArgument("whiskeyId") as String).toLong()
            val whiskey = whiskeyRepository.findById(whiskeyId)
            if (whiskey.isEmpty) {
                error("No whiskey with id $whiskeyId.")
            }
            val ratingInput = environment.getArgument("ratingInput") as Map<String, *>
            val userData = userDataRepository.getUserDataByName(securityService.authentication.get().name)

            // Create the rating
            val rating =
                Rating(
                    user = userData,
                    whiskey = whiskey.get(),
                    title = ratingInput["title"] as? String ?: error("Invalid request: rating title is missing"),
                    body = ratingInput["body"] as? String ?: error("Invalid request: rating body is missing"),
                    score = ratingInput["score"] as? Double ?: error("Invalid request: rating score is missing"),
                )
            // Validate input
            validateRating(rating)

            // Save rating to jpa
            ratingRepository.save(rating)

            // Add given attributes to rating
            val attributeInputs = environment.getArgument("attributeInputs") as List<Map<String, *>>?
            if (attributeInputs != null) {
                addAttributes(attributeInputs, rating)
            }

            return@DataFetcher ratingRepository.update(rating)
        }
    }

    fun editRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val rating = getOwnedRatingById(environment)
            val ratingInput = environment.getArgument("ratingInput") as Map<String, *>?
            with(rating) {
                title = ratingInput?.get("title") as? String ?: title
                body = ratingInput?.get("body") as? String ?: body
                score = ratingInput?.get("score") as? Double ?: score
            }

            // Validate input
            validateRating(rating)

            // Remove previous attributes from review & add updated ones
            attributeRepository.deleteAll(rating.attributes)
            rating.attributes.clear()

            // Update the score to each attribute
            val attributeInputs = environment.getArgument("attributeInputs") as List<Map<String, *>>?
            if (attributeInputs != null) {
                addAttributes(attributeInputs, rating)
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

    private fun validateRating(rating: Rating) {
        requestValidatorService.verifyScoreRange(rating.score)
        requestValidatorService.verifyMinLength(rating.title)
        requestValidatorService.verifyMinLength(rating.body, 0)
    }

    /**
     * Given a list of maps containing "id" and "score" and a rating:
     * Create a new Attribute with the given score and AttributeCategory "id".
     * Only one AttributeCategory can be given a score per rating.
     * */
    private fun addAttributes(
        inputs: List<Map<String, *>>,
        rating: Rating,
    ) {
        val seenIds: MutableList<Long> = mutableListOf()
        inputs.forEach { attributeInput: Map<String, *> ->
            // Find category by id
            val attributeCategoryId = (attributeInput["id"] as String).toLong()
            if (seenIds.contains(attributeCategoryId)) {
                error("Can not give same attribute category a score multiple times!")
            }
            seenIds.add(attributeCategoryId)

            val attributeCategory = attributeCategoryRepository.findById(attributeCategoryId)
            if (attributeCategory.isEmpty) {
                error("No attribute category with id: $attributeCategoryId")
            }

            // Verify that score is within bounds
            val attributeScore = attributeInput["score"] as Double
            requestValidatorService.verifyScoreRange(attributeScore)

            // Create a new Attribute and add it to the rating
            val attribute = Attribute(category = attributeCategory.get(), rating = rating, score = attributeScore)
            rating.attributes.add(attribute)
            attributeRepository.save(attribute)
        }
    }

    /**
     * Get a rating using environment id and verify that the user is:
     * - Logged in
     * - Is the owner of the rating or has admin privileges.
     * */
    fun getOwnedRatingById(
        environment: DataFetchingEnvironment,
        argumentName: String = "id",
    ): Rating {
        requestValidatorService.verifyAuthenticated(securityService)

        val ratingId = (environment.getArgument(argumentName) as String).toLong()
        val rating = ratingRepository.findById(ratingId)
        if (rating.isEmpty) {
            error("No rating with id $ratingId.")
        }
        val auth = securityService.authentication.get()
        if (rating.get().user?.name != auth.name && !requestValidatorService.isAdmin(securityService)) {
            error("Can't change someone else's rating")
        }
        return rating.get()
    }
}

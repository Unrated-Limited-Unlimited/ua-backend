package com.ulu.fetchers

import com.ulu.models.Rating
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton

@Singleton
class RatingFetcher () {
    fun getRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }

    fun createRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }

    fun editRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }

    fun deleteRating(): DataFetcher<Rating> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            // TODO
            return@DataFetcher null
        }
    }

}
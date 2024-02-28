package com.ulu.fetchers

import com.ulu.models.UserData
import com.ulu.repositories.UserDataRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton

@Singleton
class UserDataFetcher(private val userDataRepository: UserDataRepository) {

    fun userByIdDataFetcher(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }
}
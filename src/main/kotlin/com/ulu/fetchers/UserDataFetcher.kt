package com.ulu.fetchers

import com.ulu.models.UserData
import com.ulu.repositories.UserDataRepository
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class UserDataFetcher(private val userDataRepository: UserDataRepository, private val securityService: DefaultSecurityService) {

    fun byName(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }

    fun getLoggedInUser() : DataFetcher<UserData> {
        return DataFetcher {
            if (securityService.isAuthenticated) {
                return@DataFetcher userDataRepository.getUserDataByName(securityService.authentication.get().name)
            }
            return@DataFetcher null
        }
    }
}
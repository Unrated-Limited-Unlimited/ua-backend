package com.ulu.fetchers

import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.security.AccountCreationService
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class UserDataFetcher(
    private val userDataRepository: UserDataRepository,
    private val securityService: DefaultSecurityService,
    private val jwtRefreshTokenRepository: JwtRefreshTokenRepository
) {

    fun byName(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }

    fun getLoggedInUser(): DataFetcher<UserData> {
        return DataFetcher {
            if (securityService.isAuthenticated) {
                println(securityService.authentication.get().attributes)
                return@DataFetcher userDataRepository.getUserDataByName(securityService.authentication.get().name)
            }
            return@DataFetcher null
        }
    }

    fun editUser(): DataFetcher<UserData> {
        return DataFetcher {  dataFetchingEnvironment: DataFetchingEnvironment ->
            if (securityService.isAuthenticated) {
                val editUserMap : Map<*,*> = dataFetchingEnvironment.getArgument("user")
                val user : UserData = userDataRepository.getUserDataByName(securityService.authentication.get().name) ?: return@DataFetcher null

                val newEmail = editUserMap["email"].toString()
                if (!AccountCreationService().isValidEmail(newEmail)){
                    error("Invalid email provided.")
                }

                user.email = editUserMap["email"].toString()
                user.password = AccountCreationService().hashPassword(editUserMap["password"].toString())
                user.img = editUserMap["img"].toString()

                // Revoke all issued jwt tokens
                jwtRefreshTokenRepository.updateRevokedByUsername(securityService.authentication.get().name, true)

                return@DataFetcher userDataRepository.update(user)
            }
            return@DataFetcher null
        }
    }
}
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

    fun getUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }

    fun getLoggedInUser(): DataFetcher<UserData> {
        return DataFetcher {
            if (securityService.isAuthenticated) {
                return@DataFetcher userDataRepository.getUserDataByName(securityService.authentication.get().name)
            }
            return@DataFetcher null
        }
    }

    fun editUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            if (!securityService.isAuthenticated) {
                error("Unauthorized")
            }
            val editUserMap: Map<*, *> = dataFetchingEnvironment.getArgument("user")
            val user: UserData = userDataRepository.getUserDataByName(securityService.authentication.get().name) ?: error("User not found")

            val newEmail = editUserMap["email"] as String?
            if (newEmail != null){
                if (!AccountCreationService().isValidEmail(newEmail)) {
                    error("Invalid email provided.")
                }
                user.email = newEmail
            }

            val newPass = editUserMap["password"] as String?
            if (newPass != null){
                if (!AccountCreationService().isValidPassword(newPass)) {
                    error("Password to weak")
                }
                user.password = AccountCreationService().hashPassword(newPass)
            }
            val newImg = editUserMap["img"] as String?
            if (newImg != null) {
                user.img = newImg
            }

            // Revoke all issued jwt tokens
            jwtRefreshTokenRepository.updateRevokedByUsername(securityService.authentication.get().name, true)

            return@DataFetcher userDataRepository.update(user)
        }
    }

    fun deleteUser(): DataFetcher<String> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val user: UserData = userDataRepository.getUserDataByName(securityService.authentication.get().name)
                ?: return@DataFetcher null
            userDataRepository.delete(user)
            return@DataFetcher "deleted"
        }
    }
}
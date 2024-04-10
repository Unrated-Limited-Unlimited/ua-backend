package com.ulu.fetchers

import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.services.AccountCreationService
import com.ulu.services.RequestValidatorService
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class UserDataFetcher(
    private val userDataRepository: UserDataRepository,
    private val securityService: DefaultSecurityService,
    private val jwtRefreshTokenRepository: JwtRefreshTokenRepository,
) {
    fun getUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }

    fun getLoggedInUser(): DataFetcher<UserData> {
        return DataFetcher {
            RequestValidatorService().verifyAuthenticated(securityService)
            return@DataFetcher userDataRepository.getUserDataByName(securityService.authentication.get().name)
        }
    }

    fun editUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            RequestValidatorService().verifyAuthenticated(securityService)

            val editUserMap: Map<*, *> = dataFetchingEnvironment.getArgument("user")
            val username = securityService.authentication.get().name
            val userData: UserData = userDataRepository.getUserDataByName(username) ?: error("User not found")
            val accountCreationService = AccountCreationService()

            editUserMap["email"]?.let { newEmail ->
                if (!accountCreationService.isValidEmail(newEmail as String)) {
                    error("Invalid email provided.")
                }
                userData.email = newEmail
            }

            editUserMap["password"]?.let { newPass ->
                if (!accountCreationService.isValidPassword(newPass as String)) {
                    error("Password to weak")
                }
                userData.password = accountCreationService.hashPassword(newPass)
            }

            editUserMap["img"]?.let { newImg ->
                userData.img = newImg as String
            }
            // Revoke all issued jwt tokens
            jwtRefreshTokenRepository.updateRevokedByUsername(username, true)
            return@DataFetcher userDataRepository.update(userData)
        }
    }

    fun deleteUser(): DataFetcher<String> {
        return DataFetcher {
            RequestValidatorService().verifyAuthenticated(securityService)

            val user: UserData =
                userDataRepository.getUserDataByName(securityService.authentication.get().name)
                    ?: error("User to delete not found")
            userDataRepository.delete(user)
            return@DataFetcher "deleted"
        }
    }
}

package com.ulu.fetchers

import com.ulu.dto.RegisterRequest
import com.ulu.models.UserData
import com.ulu.repositories.JwtRefreshTokenRepository
import com.ulu.repositories.UserDataRepository
import com.ulu.services.AccountService
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
    private val accountService: AccountService,
) {
    fun getUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getUserDataByName(userName)
        }
    }

    fun getUsers(): DataFetcher<List<UserData>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val userName: String = dataFetchingEnvironment.getArgument("name")
            return@DataFetcher userDataRepository.getByNameContainsIgnoreCase(
                userName,
                RequestValidatorService().getPaging(dataFetchingEnvironment),
            ).content
        }
    }

    fun getLoggedInUser(): DataFetcher<UserData> {
        return DataFetcher {
            RequestValidatorService().verifyAuthenticated(securityService)
            return@DataFetcher userDataRepository.getUserDataByName(securityService.authentication.get().name)
        }
    }

    fun createUser(): DataFetcher<UserData> {
        return DataFetcher { environment: DataFetchingEnvironment ->
            val userMap: Map<*, *> = environment.getArgument("user")
            val registerData =
                RegisterRequest(
                    userMap["name"] as String,
                    userMap["password"] as String,
                    userMap["email"] as String,
                )
            // Create account.
            return@DataFetcher when (val result = accountService.registerNewAccount(registerData)) {
                is AccountService.AccountCreationResult.Success -> result.userData
                is AccountService.AccountCreationResult.Failure -> error(result.error)
            }
        }
    }

    fun editUser(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            RequestValidatorService().verifyAuthenticated(securityService)

            val editUserMap: Map<*, *> = dataFetchingEnvironment.getArgument("user")
            val username = securityService.authentication.get().name
            val userData: UserData = userDataRepository.getUserDataByName(username) ?: error("User not found")

            editUserMap["email"]?.let { newEmail ->
                if (!accountService.isValidEmail(newEmail as String)) {
                    error("Invalid email provided.")
                }
                userData.email = newEmail
            }

            editUserMap["password"]?.let { newPass ->
                if (!accountService.isValidPassword(newPass as String)) {
                    error("Password to weak")
                }
                userData.password = accountService.hashPassword(newPass)
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

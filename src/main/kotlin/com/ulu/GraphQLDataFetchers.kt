package com.ulu

import com.ulu.models.UserData
import com.ulu.models.Whiskey
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment

import jakarta.inject.Singleton

@Singleton
class GraphQLDataFetchers(private val dbRepository: DbRepository) {

    fun userByIdDataFetcher(): DataFetcher<UserData> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            println(dataFetchingEnvironment.toString())
            val userName: String = dataFetchingEnvironment.getArgument("name")
            dbRepository.findAllUsers()
                .firstOrNull { user: UserData -> (user.name == userName) }
        }
    }

    fun whiskeyByIdFetcher(): DataFetcher<Whiskey>{
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment? ->
            val whiskeyId : String? = dataFetchingEnvironment?.getArgument("id")

            dbRepository.findAllWhiskeys().firstOrNull{
                whiskey: Whiskey -> (whiskey.id.toString() == whiskeyId)
            }
        }
    }

    fun whiskeyFetcher(): DataFetcher<List<Whiskey>>{
        return DataFetcher {
            dbRepository.findAllWhiskeys()
        }
    }

}
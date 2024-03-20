package com.ulu.fetchers

import com.ulu.models.Label
import com.ulu.repositories.LabelRepository
import graphql.schema.DataFetcher
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class LabelFetcher(private val labelRepository: LabelRepository, private val securityService: DefaultSecurityService) {

    fun getLabels(): DataFetcher<List<Label>> {
        return DataFetcher {
            return@DataFetcher labelRepository.findAll()
        }
    }

    fun createLabel(): DataFetcher<Label> {
        return DataFetcher {
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")){
                error("You must be an admin to create new whiskeys")
            }
            val name : String = it.getArgument("name")
            return@DataFetcher labelRepository.save(Label(name=name))
        }
    }

    fun editLabel(): DataFetcher<Label> {
        return DataFetcher {
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")){
                error("You must be an admin to create new whiskeys")
            }
            val labelId = it.getArgument<String>("id").toLong()
            val name : String = it.getArgument("name")
            val label = labelRepository.findById(labelId)
            if (label.isEmpty){
                error("No label with id: $labelId found.")
            }
            label.get().name = name
            labelRepository.update(label.get())
            return@DataFetcher label.get()
        }
    }

    fun deleteLabel(): DataFetcher<String> {
        return DataFetcher {
            if (!securityService.isAuthenticated) {
                error("Unauthenticated")
            }
            if (!securityService.authentication.get().roles.contains("ROLE_ADMIN")) {
                error("You must be an admin to delete whiskeys")
            }
            labelRepository.deleteById(it.getArgument("id"))
            return@DataFetcher "deleted"
        }
    }

}
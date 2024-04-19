package com.ulu.fetchers

import com.ulu.models.AttributeCategory
import com.ulu.repositories.AttributeCategoryRepository
import com.ulu.services.RequestValidatorService
import graphql.schema.DataFetcher
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class AttributeCategoryFetcher(
    private val attributeCategoryRepository: AttributeCategoryRepository,
    private val requestValidatorService: RequestValidatorService,
    private val securityService: DefaultSecurityService,
) {
    fun getAttributeCategories(): DataFetcher<List<AttributeCategory>> {
        return DataFetcher {
            val categories = attributeCategoryRepository.findAll()
            categories.forEach { it.calculateAvgScore() }
            return@DataFetcher categories
        }
    }

    fun createAttributeCategory(): DataFetcher<AttributeCategory> {
        return DataFetcher {
            requestValidatorService.verifyAdmin(securityService)
            val name: String = it.getArgument("name")
            if (attributeCategoryRepository.existsByName(name)) {
                error("AttributeCategory with identical name already exists")
            }
            return@DataFetcher attributeCategoryRepository.save(AttributeCategory(name = name))
        }
    }

    fun editAttributeCategory(): DataFetcher<AttributeCategory> {
        return DataFetcher {
            requestValidatorService.verifyAdmin(securityService)

            val attributeCategoryId = it.getArgument<String>("id").toLong()
            val name: String = it.getArgument("name")
            val attributeCategory = attributeCategoryRepository.findById(attributeCategoryId)
            if (attributeCategory.isEmpty) {
                error("No attributeCategory with id: $attributeCategoryId found.")
            }
            if (attributeCategoryRepository.existsByName(name)) {
                error("attributeCategory with identical name already exists")
            }
            attributeCategory.get().name = name
            attributeCategoryRepository.update(attributeCategory.get())
            return@DataFetcher attributeCategory.get()
        }
    }

    fun deleteAttributeCategory(): DataFetcher<String> {
        return DataFetcher {
            requestValidatorService.verifyAdmin(securityService)

            attributeCategoryRepository.deleteById(it.getArgument("id"))
            return@DataFetcher "deleted"
        }
    }
}

package com.ulu.factories

import com.ulu.fetchers.*
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Factory that maps the Query and Mutations defined in the graphql schema to functions/fetchers
 * */
@Factory
class GraphQLFactory {
    @Bean
    @Singleton
    fun graphQL(
        resourceResolver: ResourceResolver,
        whiskeyFetcher: WhiskeyFetcher,
        userDataFetcher: UserDataFetcher,
        ratingFetcher: RatingFetcher,
        thumbFetcher: ThumbFetcher,
        attributeCategoryFetcher: AttributeCategoryFetcher,
    ): GraphQL {
        val schemaParser = SchemaParser()

        val typeRegistry = TypeDefinitionRegistry()
        val graphqlSchema = resourceResolver.getResourceAsStream("classpath:schema.graphqls")

        return if (graphqlSchema.isPresent) {
            typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(graphqlSchema.get()))))

            // Link schema request to dataFetcher functions
            val runtimeWiring = buildRuntimeWiring(whiskeyFetcher, userDataFetcher, ratingFetcher, thumbFetcher, attributeCategoryFetcher)

            val schemaGenerator = SchemaGenerator()
            val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)
            GraphQL.newGraphQL(graphQLSchema).build()
        } else {
            LOG.debug("No GraphQL services found, returning empty schema")
            GraphQL.Builder(GraphQLSchema.newSchema().build()).build()
        }
    }

    /**
     * Register the Query and Mutations defined in schema.graphqls to corresponding fetchers
     * */
    private fun buildRuntimeWiring(
        whiskeyFetcher: WhiskeyFetcher,
        userDataFetcher: UserDataFetcher,
        ratingFetcher: RatingFetcher,
        thumbFetcher: ThumbFetcher,
        attributeCategoryFetcher: AttributeCategoryFetcher,
    ): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring().apply {
            type("Query") {
                // Single objects
                it.dataFetcher("getUser", userDataFetcher.getUser())
                it.dataFetcher("getLoggedInUser", userDataFetcher.getLoggedInUser())
                it.dataFetcher("getWhiskey", whiskeyFetcher.getWhiskey())
                it.dataFetcher("getRating", ratingFetcher.getRating())
                it.dataFetcher("getThumb", thumbFetcher.getThumb())
                // Multiple objects
                it.dataFetcher("getUsers", userDataFetcher.getUsers())
                it.dataFetcher("getWhiskeys", whiskeyFetcher.getWhiskeys())
                it.dataFetcher("getAttributeCategories", attributeCategoryFetcher.getAttributeCategories())
            }
            type("Mutation") {
                // User
                it.dataFetcher("createUser", userDataFetcher.createUser())
                it.dataFetcher("editUser", userDataFetcher.editUser())
                it.dataFetcher("deleteUser", userDataFetcher.deleteUser())
                // Rating
                it.dataFetcher("createRating", ratingFetcher.createRating())
                it.dataFetcher("editRating", ratingFetcher.editRating())
                it.dataFetcher("deleteRating", ratingFetcher.deleteRating())
                // Thumb / likes
                it.dataFetcher("createThumb", thumbFetcher.createThumb())
                it.dataFetcher("editThumb", thumbFetcher.editThumb())
                it.dataFetcher("deleteThumb", thumbFetcher.deleteThumb())
                // Whiskey (requires admin)
                it.dataFetcher("createWhiskey", whiskeyFetcher.createWhiskey())
                it.dataFetcher("editWhiskey", whiskeyFetcher.editWhiskey())
                it.dataFetcher("deleteWhiskey", whiskeyFetcher.deleteWhiskey())
                // Attribute categories (Requires admin)
                it.dataFetcher("createAttributeCategory", attributeCategoryFetcher.createAttributeCategory())
                it.dataFetcher("editAttributeCategory", attributeCategoryFetcher.editAttributeCategory())
                it.dataFetcher("deleteAttributeCategory", attributeCategoryFetcher.deleteAttributeCategory())
            }
        }.build()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphQLFactory::class.java)
    }
}

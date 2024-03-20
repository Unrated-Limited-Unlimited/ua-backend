package com.ulu.factories

import com.ulu.fetchers.*
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.io.ResourceResolver
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import jakarta.inject.Singleton

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
        labelFetcher: LabelFetcher
    ): GraphQL {
        val schemaParser = SchemaParser()

        val typeRegistry = TypeDefinitionRegistry()
        val graphqlSchema = resourceResolver.getResourceAsStream("classpath:schema.graphqls")

        return if (graphqlSchema.isPresent) {
            typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(graphqlSchema.get()))))

            // Link schema request to dataFetcher functions
            val runtimeWiring = RuntimeWiring.newRuntimeWiring()

                // User
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getUser", userDataFetcher.getUser())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getLoggedInUser", userDataFetcher.getLoggedInUser())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("editUser", userDataFetcher.editUser())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("deleteUser", userDataFetcher.deleteUser())
                )

                // Whiskey
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getWhiskeys", whiskeyFetcher.getWhiskeys())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getWhiskey", whiskeyFetcher.getWhiskey())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createWhiskey", whiskeyFetcher.createWhiskey())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("editWhiskey", whiskeyFetcher.editWhiskey())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("deleteWhiskey", whiskeyFetcher.deleteWhiskey())
                )

                // Rating
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getRating", ratingFetcher.getRating())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createRating", ratingFetcher.createRating())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("editRating", ratingFetcher.editRating())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("deleteRating", ratingFetcher.deleteRating())
                )

                // Thumb
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getThumb", thumbFetcher.getThumb())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createThumb", thumbFetcher.createThumb())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("editThumb", thumbFetcher.editThumb())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("deleteThumb", thumbFetcher.deleteThumb())
                )

                //Label
                .type(
                    TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getLabels", labelFetcher.getLabels())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createLabel", labelFetcher.createLabel())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("editLabel", labelFetcher.editLabel())
                )
                .type(
                    TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("deleteLabel", labelFetcher.deleteLabel())
                )

                // Finish/build the schema
                .build()

            val schemaGenerator = SchemaGenerator()
            val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)
            GraphQL.newGraphQL(graphQLSchema).build()
        } else {
            LOG.debug("No GraphQL services found, returning empty schema")
            GraphQL.Builder(GraphQLSchema.newSchema().build()).build()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphQLFactory::class.java)
    }
}
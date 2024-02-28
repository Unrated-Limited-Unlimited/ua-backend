package com.ulu.factories

import com.ulu.fetchers.UserDataFetcher
import com.ulu.fetchers.WhiskeyFetcher
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
    fun graphQL(resourceResolver: ResourceResolver, whiskeyFetcher: WhiskeyFetcher, userDataFetcher: UserDataFetcher): GraphQL {
        val schemaParser = SchemaParser()

        val typeRegistry = TypeDefinitionRegistry()
        val graphqlSchema = resourceResolver.getResourceAsStream("classpath:schema.graphqls")

        return if (graphqlSchema.isPresent) {
            typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(graphqlSchema.get()))))

            // Link schema request to dataFetcher functions
            val runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("getUser", userDataFetcher.byName()))

                .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("getWhiskeys", whiskeyFetcher.allBySortType()))

                .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("getWhiskey", whiskeyFetcher.byId()))

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
package com.ulu

import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false)
internal class OpenApiGeneratedTest {
    @Test
    fun buildGeneratesOpenApi(resourceLoader: ResourceLoader) {
        assertTrue(resourceLoader.getResource("META-INF/swagger/unrated-1.0.yml").isPresent)
    }

    @Test
    fun buildGeneratesOpenApiHtml(resourceLoader: ResourceLoader) {
        assertTrue(resourceLoader.getResource("META-INF/swagger/views/swagger-ui/index.html").isPresent)
    }
}

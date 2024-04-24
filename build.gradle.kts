plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.3.2"
    id("io.micronaut.test-resources") version "4.3.2"
    id("io.micronaut.aot") version "4.3.2"
}

version = "0.1"
group = "com.ulu"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.security:micronaut-security-annotations")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.graphql:micronaut-graphql")

    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.yaml:snakeyaml")

    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.data:micronaut-data-tx-hibernate")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compileOnly("io.micronaut:micronaut-http-client")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")

    runtimeOnly("com.h2database:h2") // For testing JPA local

    testImplementation("io.micronaut:micronaut-http-client")
    aotPlugins(platform("io.micronaut.platform:micronaut-platform:4.3.2"))
    aotPlugins("io.micronaut.security:micronaut-security-aot")
}

application {
    mainClass.set("com.ulu.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("manual")
    }
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.ulu.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
        configurationProperties.put("micronaut.security.jwks.enabled", "false")
    }
}

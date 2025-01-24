plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
}

dependencies {
    // hotlibs
    implementation(platform(libs.hotlibs.platform))
    implementation(libs.hotlibs.core)
    implementation(libs.hotlibs.logging)
    implementation(libs.hotlibs.serialization)

    implementation(libs.nocommons)

    // Kafka
    implementation(libs.kafka.streams)

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.metrics.micrometer)

    // Metrics
    implementation(libs.micrometer.registry.prometheus)

    // Jackson
    implementation(libs.bundles.jackson)

    // DigiHoT
    implementation(libs.hotlibs.core)
    implementation(libs.hm.contract.pdl.avro)

    // Logging
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.bundles.logging.runtime)

    // Test
    testImplementation(libs.bundles.test)
    testImplementation(libs.kafka.streams.test.utils)
    testImplementation(libs.handlebars)
    testImplementation(libs.jackson.dataformat.yaml)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin.asProvider())
            dependencies {
                implementation(libs.handlebars)
                implementation(libs.hotlibs.test)
                implementation(libs.jackson.dataformat.yaml)
                implementation(libs.kafka.streams.test.utils)
                implementation(libs.kotest.assertions.json)
                implementation(libs.ktor.server.test.host)
                implementation(libs.nimbus.jose.jwt)
            }
        }
    }
}

application { mainClass.set("no.nav.hjelpemidler.oppgave.listener.ApplicationKt") }

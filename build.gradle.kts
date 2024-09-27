plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.nocommons)

    // Kafka
    implementation(libs.kafka.streams)
    implementation(libs.kafka.streams.avro.serde)
    constraints {
        implementation(libs.commons.compress)
    }

    // Ktor
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

kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }

application { mainClass.set("no.nav.hjelpemidler.oppgave.listener.ApplicationKt") }

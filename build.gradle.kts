plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
}

dependencies {
    // hotlibs
    implementation(platform(libs.hotlibs.platform))
    implementation(libs.hotlibs.streams)
    implementation(libs.ktor.server.sse)
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
            }
        }
    }
}

application { mainClass.set("no.nav.hjelpemidler.oppgave.listener.ApplicationKt") }

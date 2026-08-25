plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
}

dependencies {
    // hotlibs
    implementation(platform(libs.hotlibs.platform))
    implementation(libs.hotlibs.oppgave)
    implementation(libs.hotlibs.streams)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

testing {
    suites {
        @Suppress("UnstableApiUsage")
        val test = named<JvmTestSuite>("test") {
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.hotlibs.test)

                implementation(libs.handlebars)
                implementation(libs.jackson.dataformat.yaml)
                implementation(libs.kafka.streams.test.utils)
                implementation(libs.kotest.assertions.json)
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}

application { mainClass = "no.nav.hjelpemidler.oppgave.listener.ApplicationKt" }

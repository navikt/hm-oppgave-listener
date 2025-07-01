package no.nav.hjelpemidler.oppgave.listener

import no.nav.hjelpemidler.configuration.EnvironmentVariable

object Configuration {
    val HTTP_PORT by EnvironmentVariable(transform = String::toInt)
    val KAFKA_APPLICATION_ID by EnvironmentVariable
    val KAFKA_RAPID_TOPIC by EnvironmentVariable
    val OPPGAVE_TOPIC by EnvironmentVariable
}

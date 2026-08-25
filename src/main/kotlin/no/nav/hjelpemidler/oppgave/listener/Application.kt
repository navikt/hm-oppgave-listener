package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication

private val log = KotlinLogging.logger {}

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        topology {
            oppgavehendelse()
        }
    }.start(wait = true)
}

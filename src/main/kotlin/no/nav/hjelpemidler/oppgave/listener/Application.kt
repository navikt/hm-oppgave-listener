package no.nav.hjelpemidler.oppgave.listener

import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        topology {
            oppgavehendelse()
        }
    }.start()
}

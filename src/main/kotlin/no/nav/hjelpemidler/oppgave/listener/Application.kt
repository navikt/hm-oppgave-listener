package no.nav.hjelpemidler.oppgave.listener

import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        val broker = MessageBroker(ValkeyConfiguration("broker"))

        topology {
            oppgavehendelse(broker)
        }

        application {
            install(SSE)

            routing {
                sse("/events") {
                    heartbeat()
                    broker
                        .subscribe("hm-oppgave-event")
                        .collect {
                            send(it, "hm-oppgave-event")
                        }
                }
            }
        }
    }.start(wait = true)
}

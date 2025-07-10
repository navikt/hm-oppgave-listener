package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication

private val log = KotlinLogging.logger {}

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        val brokerConfiguration = ValkeyConfiguration("broker")
        log.info { "Broker configuration, uri: '${brokerConfiguration.uri}'" }
        val broker = MessageBroker(brokerConfiguration)

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

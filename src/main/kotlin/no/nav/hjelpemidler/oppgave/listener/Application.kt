package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.events.EventHandler
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import no.nav.hjelpemidler.oppgave.listener.broker.MessageBroker
import no.nav.hjelpemidler.oppgave.listener.broker.ValkeyMessageBroker
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger {}

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        val broker: MessageBroker = ValkeyMessageBroker(instanceName = "broker")

        topology {
            oppgavehendelse(broker)
        }

        application {
            install(SSE)

            routing {
                sse("/events") {
                    heartbeat { period = 10.seconds }
                    broker
                        .subscribe("hm-oppgave-event")
                        .collect { send(it, "hm-oppgave-event") }
                }
            }

            var stopping: EventHandler<Application> = {}
            stopping = { _ ->
                broker.close()
                monitor.unsubscribe(ApplicationStopping, stopping)
            }
            monitor.subscribe(ApplicationStopping, stopping)
        }
    }.start(wait = true)
}

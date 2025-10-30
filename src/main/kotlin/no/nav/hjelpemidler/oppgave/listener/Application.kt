package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.install
import io.ktor.server.plugins.di.DI
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import no.nav.hjelpemidler.oppgave.listener.broker.MessageBroker
import no.nav.hjelpemidler.oppgave.listener.broker.ValkeyMessageBroker
import no.nav.hjelpemidler.oppgave.listener.oppgave.UtgåendeOppgaveEvent
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.kafkaStreamsApplication
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger {}

fun main() {
    kafkaStreamsApplication(
        applicationId = Configuration.KAFKA_APPLICATION_ID,
        port = Configuration.HTTP_PORT,
    ) {
        topology {
            oppgavehendelse()
        }

        application {
            install(DI)
            install(SSE)

            dependencies {
                provide<MessageBroker> { ValkeyMessageBroker(instanceName = "broker") }
            }

            val broker: MessageBroker by dependencies

            routing {
                sse("/events") {
                    heartbeat { period = 10.seconds }
                    broker
                        .subscribe(UtgåendeOppgaveEvent.EVENT_NAME)
                        .collect { send(it) }
                }
            }
        }
    }.start(wait = true)
}

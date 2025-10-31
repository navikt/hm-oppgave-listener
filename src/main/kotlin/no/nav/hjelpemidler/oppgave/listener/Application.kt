package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.events.EventHandler
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.plugins.di.DI
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.kafka.createKafkaConsumer
import no.nav.hjelpemidler.oppgave.listener.broker.MessageBroker
import no.nav.hjelpemidler.oppgave.listener.broker.ValkeyMessageBroker
import no.nav.hjelpemidler.oppgave.listener.oppgave.UtgåendeOppgaveEvent
import no.nav.hjelpemidler.oppgave.listener.oppgave.UtgåendeOppgaveServerSentEvent
import no.nav.hjelpemidler.oppgave.listener.oppgave.asFlow
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.serialization.jackson.jsonToTree
import no.nav.hjelpemidler.serialization.jackson.treeToValue
import no.nav.hjelpemidler.serialization.jackson.valueToJson
import no.nav.hjelpemidler.streams.kafkaStreamsApplication
import org.apache.kafka.clients.consumer.ConsumerConfig
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

            val consumerJob = launch {
                val consumer = createKafkaConsumer {
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
                    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true)
                    put(ConsumerConfig.GROUP_ID_CONFIG, "hm-oppgave-listener-sse-v1")
                }
                consumer.subscribe(setOf("teamdigihot.hm-soknadsbehandling-v1"))
                consumer.asFlow()
                    .mapNotNull {
                        try {
                            jsonToTree(it.value())
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .filter { it["eventName"]?.textValue() == UtgåendeOppgaveEvent.EVENT_NAME }
                    .mapNotNull {
                        try {
                            treeToValue<UtgåendeOppgaveEvent>(it)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .collect {
                        val message = valueToJson(UtgåendeOppgaveServerSentEvent(it))
                        broker.publish(UtgåendeOppgaveEvent.EVENT_NAME, message)
                    }
            }

            var stopping: EventHandler<Application> = {}
            stopping = { _ ->
                consumerJob.cancel()
                monitor.unsubscribe(ApplicationStopping, stopping)
            }
            monitor.subscribe(ApplicationStopping, stopping)

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

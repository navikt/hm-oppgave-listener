package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.di.DI
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.kafka.createKafkaConsumer
import no.nav.hjelpemidler.oppgave.listener.broker.MessageBroker
import no.nav.hjelpemidler.oppgave.listener.broker.NoOpMessageBroker
import no.nav.hjelpemidler.oppgave.listener.broker.ValkeyMessageBroker
import no.nav.hjelpemidler.oppgave.listener.oppgave.InternOppgavehendelse
import no.nav.hjelpemidler.oppgave.listener.oppgave.InternOppgavehendelseSSE
import no.nav.hjelpemidler.oppgave.listener.oppgave.asFlow
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.serialization.jackson.jsonToTree
import no.nav.hjelpemidler.serialization.jackson.stringValueOrNull
import no.nav.hjelpemidler.serialization.jackson.treeToValue
import no.nav.hjelpemidler.serialization.jackson.valueToJson
import no.nav.hjelpemidler.streams.kafkaStreamsApplication
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
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

            if (Environment.current.isLocal) {
                kafkaConsumer()
            }

            dependencies {
                provide<MessageBroker> {
                    if (Environment.current.isLocal) {
                        ValkeyMessageBroker(instanceName = "broker")
                    } else {
                        NoOpMessageBroker()
                    }
                }
            }

            val broker: MessageBroker by dependencies
            routing {
                sse("/events") {
                    heartbeat { period = 10.seconds }
                    broker
                        .subscribe(InternOppgavehendelse.EVENT_NAME)
                        .collect(::send)
                }
            }
        }
    }.start(wait = true)
}

fun Application.kafkaConsumer() {
    val broker: MessageBroker by dependencies
    launch(Dispatchers.IO) {
        val consumer = createKafkaConsumer {
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            put(ConsumerConfig.GROUP_ID_CONFIG, "hm-oppgave-listener-sse-v1")
        }
        consumer.subscribe(setOf(Configuration.KAFKA_RAPID_TOPIC))
        consumer.asFlow()
            .mapNotNull { record ->
                val message = try {
                    jsonToTree(record.value())
                } catch (_: Exception) {
                    return@mapNotNull null
                }

                if (message["eventName"]?.stringValueOrNull() != InternOppgavehendelse.EVENT_NAME) {
                    return@mapNotNull null
                }

                val oppgavehendelse = try {
                    treeToValue<InternOppgavehendelse>(message)
                } catch (_: Exception) {
                    return@mapNotNull null
                }

                record to oppgavehendelse
            }
            .collect { (record, event) ->
                val message = valueToJson(InternOppgavehendelseSSE(event))
                broker.publish(InternOppgavehendelse.EVENT_NAME, message)
                consumer.commitSync(mapOf(record.topicPartition() to record.offsetAndMetadata()))
            }
    }
}

private fun ConsumerRecord<*, *>.topicPartition() = TopicPartition(topic(), partition())
private fun ConsumerRecord<*, *>.offsetAndMetadata() = OffsetAndMetadata(offset() + 1)

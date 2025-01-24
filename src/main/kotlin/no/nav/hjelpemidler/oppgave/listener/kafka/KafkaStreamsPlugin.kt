package no.nav.hjelpemidler.oppgave.listener.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.events.EventDefinition
import io.ktor.events.EventHandler
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KafkaStreams.State

private val log = KotlinLogging.logger {}

class KafkaStreamsPluginConfiguration {
    lateinit var kafkaStreams: KafkaStreams
}

val KafkaStreamsPlugin = createApplicationPlugin("KafkaStreamsPlugin", ::KafkaStreamsPluginConfiguration) {
    val kafkaStreams = pluginConfig.kafkaStreams

    kafkaStreams.setStateListener { newState, oldState ->
        application.monitor.raise(
            KafkaStreamsStateTransitionEvent,
            KafkaStreamsStateTransition(newState, oldState),
        )
    }

    val started: EventHandler<Application> = { _ ->
        kafkaStreams.cleanUp()
        kafkaStreams.start()
        log.info { "Kafka Streams startet" }
    }
    var stopped: EventHandler<Application> = {}
    stopped = { _ ->
        kafkaStreams.close()
        log.info { "Kafka Streams stoppet" }
        application.monitor.unsubscribe(ApplicationStarted, started)
        application.monitor.unsubscribe(ApplicationStopped, stopped)
    }

    on(MonitoringEvent(ApplicationStarted), started)
    on(MonitoringEvent(ApplicationStopped), stopped)

    onCall { _ ->
        when (val state = kafkaStreams.state()) {
            State.RUNNING -> log.debug { "state: $state" }
            else -> log.info { "state: $state" }
        }
    }
}

data class KafkaStreamsStateTransition(val newState: State, val oldState: State)

val KafkaStreamsStateTransitionEvent: EventDefinition<KafkaStreamsStateTransition> = EventDefinition()

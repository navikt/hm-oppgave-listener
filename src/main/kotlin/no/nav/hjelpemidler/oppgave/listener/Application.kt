package no.nav.hjelpemidler.oppgave.listener

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse
import no.nav.hjelpemidler.streams.KafkaStreamsPlugin
import no.nav.hjelpemidler.streams.health
import no.nav.hjelpemidler.streams.kafkaStreams

fun main() {
    embeddedServer(Netty, Configuration.HTTP_PORT, module = Application::main).start()
}

fun Application.main() {
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = meterRegistry
    }

    val kafkaStreams = kafkaStreams(Configuration.KAFKA_APPLICATION_ID) { oppgavehendelse() }
    install(KafkaStreamsPlugin) {
        this.kafkaStreams = kafkaStreams
    }

    routing {
        health(kafkaStreams)

        get("/metrics") {
            call.respond(meterRegistry.scrape())
        }
    }
}

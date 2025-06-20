package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
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
import no.nav.hjelpemidler.logging.teamInfo
import no.nav.hjelpemidler.oppgave.listener.kafka.KafkaStreamsPlugin
import no.nav.hjelpemidler.oppgave.listener.kafka.kafkaStreams
import no.nav.hjelpemidler.oppgave.listener.oppgave.oppgavehendelse

fun main() {
    embeddedServer(Netty, Configuration.HTTP_PORT, module = Application::main).start()
}

private val log = KotlinLogging.logger { }

fun Application.main() {

    log.info { "TEST_LOG" }
    log.teamInfo { "TEST_TEAM_LOGS" }

    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = meterRegistry
    }

    val kafkaStreams = kafkaStreams {
        oppgavehendelse()
    }
    install(KafkaStreamsPlugin) {
        this.kafkaStreams = kafkaStreams
    }

    routing {
        get("/isalive") {
            call.respond(HttpStatusCode.OK)
        }
        get("/isready") {
            call.respond(HttpStatusCode.OK)
        }
        get("/metrics") {
            call.respond(meterRegistry.scrape())
        }
    }
}

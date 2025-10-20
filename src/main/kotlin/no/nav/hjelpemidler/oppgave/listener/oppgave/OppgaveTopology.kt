package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.broker.MessageBroker
import no.nav.hjelpemidler.streams.serialization.jsonSerde
import no.nav.hjelpemidler.streams.serialization.serde
import no.nav.hjelpemidler.streams.toRapid
import no.nav.hjelpemidler.time.toInstant
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import java.time.Instant

private val log = KotlinLogging.logger {}

fun StreamsBuilder.oppgavehendelse(broker: MessageBroker) = this
    .stream(
        Configuration.OPPGAVE_TOPIC,
        Consumed.with(serde<Long>(), jsonSerde<InnkommendeOppgaveEvent>())
    )
    .filter { _, oppgaveEvent -> oppgaveEvent.oppgave.harTemaHjelpemidler }
    .peek { key, oppgaveEvent ->
        if (Environment.current.isDev) {
            log.debug { "Mottok oppgavehendelse: $oppgaveEvent, key: $key" }
        }
    }
    .selectKey { oppgaveId, _ -> oppgaveId.toString() }
    .mapValues(::UtgåendeOppgaveEvent)
    /*
    .peek { _, oppgaveEvent ->
        broker.publish(oppgaveEvent.eventName, UtgåendeOppgaveServerSentEvent(oppgaveEvent))
    }
    */
    .toRapid<String, UtgåendeOppgaveEvent>()

data class UtgåendeOppgaveServerSentEvent(
    val hendelsestype: String,
    val tidspunkt: Instant,
    val oppgaveId: String,
    val versjon: Int,
) {
    constructor(event: UtgåendeOppgaveEvent) : this(
        hendelsestype = event.hendelse.hendelsestype,
        tidspunkt = event.hendelse.tidspunkt.toInstant(),
        oppgaveId = event.oppgave.oppgaveId,
        versjon = event.oppgave.versjon,
    )
}

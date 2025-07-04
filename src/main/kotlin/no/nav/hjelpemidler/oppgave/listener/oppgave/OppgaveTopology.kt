package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.streams.serialization.jsonSerde
import no.nav.hjelpemidler.streams.serialization.serde
import no.nav.hjelpemidler.streams.toRapid
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

private val log = KotlinLogging.logger {}

fun StreamsBuilder.oppgavehendelse() = this
    .stream(
        Configuration.OPPGAVE_TOPIC,
        Consumed.with(serde<Long>(), jsonSerde<InnkommendeOppgaveEvent>())
    )
    .filter { _, oppgaveEvent -> oppgaveEvent.oppgave.harTemaHjelpemidler }
    .peek { key, oppgaveEvent ->
        log.debug { "Mottok oppgavehendelse: $oppgaveEvent, key: $key" }
    }
    .selectKey { oppgaveId, _ -> oppgaveId.toString() }
    .mapValues(::UtgåendeOppgaveEvent)
    .toRapid<String, UtgåendeOppgaveEvent>()

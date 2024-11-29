package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.kafka.jsonSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.longSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.toRapid
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

private val log = KotlinLogging.logger {}

fun StreamsBuilder.oppgavehendelse() = this
    .stream(
        Configuration.OPPGAVE_TOPIC,
        Consumed.with(longSerde, jsonSerde<OppgaveEvent>())
    )
    .filter { _, oppgaveEvent -> oppgaveEvent.oppgave.erHjelpemiddel }
    .peek { key, oppgaveEvent ->
        log.info { "Mottok oppgavehendelse: $oppgaveEvent, key: $key" }
    }
    .selectKey { oppgaveId, _ -> oppgaveId.toString() }
    .toRapid() // TODO: fjern miljø-test når vi skal sende meldinger til egen rapid

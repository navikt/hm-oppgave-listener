package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.kafka.jsonSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.stringSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.toRapid
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

private val log = KotlinLogging.logger {}

fun StreamsBuilder.oppgavehendelse() = this
    .stream(
        Configuration.OPPGAVE_TOPIC,
        Consumed.with(stringSerde, jsonSerde<OppgaveEvent>())
    )
    .filter { _, oppgave -> oppgave.oppgave.erHjelpemiddel }
    .peek { _, oppgave ->
        log.info { "Mottok melding om oppgave: $oppgave" }
    }
    .toRapid() // TODO: fjern miljø-test når vi skal sende meldinger til egen rapid

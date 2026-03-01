package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.databind.JsonMappingException
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.streams.serialization.jsonSerde
import no.nav.hjelpemidler.streams.serialization.serde
import no.nav.hjelpemidler.streams.toRapid
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

private val log = KotlinLogging.logger {}

fun StreamsBuilder.oppgavehendelse() = this
    .stream(
        Configuration.OPPGAVE_TOPIC,
        Consumed.with(serde<Long>(), EksternOppgavehendelseSerde)
    )
    .filter { _, oppgavehendelse -> oppgavehendelse != null }
    .mapValues { it!! }
    .filter { _, oppgavehendelse -> oppgavehendelse.oppgave.kategorisering.run { isTemaHjelpemidler && isOppgavetypeHotsak } }
    .peek { key, oppgavehendelse ->
        if (Environment.current.isDev) {
            log.debug { "Mottok oppgavehendelse: $oppgavehendelse, key: $key" }
        } else {
            log.info { "Mottok oppgavehendelse, oppgaveId: ${oppgavehendelse.oppgave.oppgaveId}, hendelsestype: ${oppgavehendelse.hendelse.hendelsestype}, key: $key" }
        }
    }
    .selectKey { oppgaveId, _ -> oppgaveId.toString() }
    .mapValues(::InternOppgavehendelse)
    .toRapid<String, InternOppgavehendelse>()

object EksternOppgavehendelseSerde : Serde<EksternOppgavehendelse?> {
    private val wrapped = jsonSerde<EksternOppgavehendelse>()
    private val serializer = wrapped.serializer()
    private val deserializer = wrapped.deserializer()
    override fun serializer(): Serializer<EksternOppgavehendelse?> = serializer
    override fun deserializer(): Deserializer<EksternOppgavehendelse?> = Deserializer { topic, data ->
        try {
            deserializer.deserialize(topic, data)
        } catch (e: Exception) {
            if (Environment.current.isDev && e is JsonMappingException) {
                log.warn(e) { "Hopper over oppgavehendelse med ugyldig JSON, data: '${data.toString(Charsets.UTF_8)}'" }
                null
            } else {
                throw e
            }
        }
    }
}

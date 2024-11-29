package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.jsonMapper
import no.nav.hjelpemidler.oppgave.listener.kafka.jsonSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.longSerde
import no.nav.hjelpemidler.oppgave.listener.kafka.stringSerde
import no.nav.hjelpemidler.oppgave.listener.test.asSequence
import no.nav.hjelpemidler.oppgave.listener.test.testTopology
import java.time.LocalDateTime
import kotlin.test.Test

class OppgaveTopologyTest {
    private val oppgaveEventSerde = jsonSerde<OppgaveEvent>()

    private val driver = testTopology {
        oppgavehendelse()
    }

    private val inputTopic = driver.createInputTopic(
        Configuration.OPPGAVE_TOPIC,
        longSerde.serializer(),
        oppgaveEventSerde.serializer(),
    )
    private val outputTopic = driver.createOutputTopic(
        Configuration.KAFKA_RAPID_TOPIC,
        stringSerde.deserializer(),
        oppgaveEventSerde.deserializer(),
    )

    @Test
    fun `Skal transformere melding om oppgave og sende svaret videre på rapid`() {
        val oppgaveId = 123L
        val erHjelpemiddel = true

        inputTopic.pipeInput(
            oppgaveId, OppgaveEvent(
                hendelse = Hendelse(
                    hendelsestype = "OPPRETTET",
                    tidspunkt = LocalDateTime.of(2024, 9,20, 10, 0,0),
                ),
                utførtAv = UtførtAv(
                    navIdent = "Z999999",
                    enhetsnummer = "9999",
                ),
                Oppgave(
                    oppgaveId = oppgaveId.toString(),
                    versjon = 1,
                    tilordning = jsonMapper.createObjectNode(),
                    kategorisering = Kategorisering(
                        tema = "HJE",
                        oppgavetype = "OPP",
                        behandlingstema = "HJE",
                        behandlingstype = "HJE",
                        prioritet = Kategorisering.Prioritet.NORMAL,
                    ),
                    behandlingsperiode = jsonMapper.createObjectNode(),
                    bruker = Bruker(
                        ident = "12345678901",
                        identType = "FOLKEREGISTERIDENT",
                    ),
                )
            )
        )

        val record = outputTopic.asSequence().single()
        record.key shouldBe oppgaveId.toString()

        val value = record.value
        value.oppgave.oppgaveId shouldBe oppgaveId.toString()
        value.oppgave.erHjelpemiddel shouldBe erHjelpemiddel
    }
}
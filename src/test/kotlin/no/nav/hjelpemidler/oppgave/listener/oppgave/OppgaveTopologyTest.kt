package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.test.asSequence
import no.nav.hjelpemidler.oppgave.listener.test.testTopology
import no.nav.hjelpemidler.streams.serialization.jsonSerde
import no.nav.hjelpemidler.streams.serialization.serde
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

class OppgaveTopologyTest {
    private val innkommendeOppgaveEventSerde = jsonSerde<InnkommendeOppgaveEvent>()
    private val utgåendeOppgaveEventSerde = jsonSerde<UtgåendeOppgaveEvent>()

    private val driver = testTopology {
        oppgavehendelse()
    }

    private val inputTopic = driver.createInputTopic(
        Configuration.OPPGAVE_TOPIC,
        serde<Long>().serializer(),
        innkommendeOppgaveEventSerde.serializer(),
    )
    private val outputTopic = driver.createOutputTopic(
        Configuration.KAFKA_RAPID_TOPIC,
        serde<String>().deserializer(),
        utgåendeOppgaveEventSerde.deserializer(),
    )

    @Test
    fun `Skal transformere melding om oppgave og sende svaret videre på rapid`() {
        val oppgaveId = 123L
        val erHjelpemiddel = true

        inputTopic.pipeInput(
            oppgaveId, InnkommendeOppgaveEvent(
                hendelse = Hendelse(
                    hendelsestype = "OPPRETTET",
                    tidspunkt = LocalDateTime.of(2024, 9, 20, 10, 0, 0),
                ),
                utførtAv = UtførtAv(
                    navIdent = "Z999999",
                    enhetsnummer = "9999",
                ),
                Oppgave(
                    oppgaveId = oppgaveId.toString(),
                    versjon = 1,
                    tilordning = Tilordning(
                        enhetsnummer = "9999",
                        enhetsmappeId = "9999",
                        navIdent = "Z999999",
                    ),
                    kategorisering = Kategorisering(
                        tema = "HJE",
                        oppgavetype = "OPP",
                        behandlingstema = "HJE",
                        behandlingstype = "HJE",
                        prioritet = Kategorisering.Prioritet.NORMAL,
                    ),
                    behandlingsperiode = Behandlingsperiode(
                        aktiv = LocalDate.of(2024, 9, 20),
                        frist = LocalDate.of(2024, 9, 27),
                    ),
                    bruker = Bruker(
                        ident = "12345678901",
                        identType = Bruker.IdentType.FOLKEREGISTERIDENT,
                    ),
                )
            )
        )

        val record = outputTopic.asSequence().single()
        record.key shouldBe oppgaveId.toString()

        val value = record.value
        value.oppgave.oppgaveId shouldBe oppgaveId.toString()
        value.oppgave.harTemaHjelpemidler shouldBe erHjelpemiddel
    }
}

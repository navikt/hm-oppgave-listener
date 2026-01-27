package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse
import no.nav.hjelpemidler.oppgave.listener.Configuration
import no.nav.hjelpemidler.oppgave.listener.test.asSequence
import no.nav.hjelpemidler.oppgave.listener.test.testTopology
import no.nav.hjelpemidler.streams.serialization.jsonSerde
import no.nav.hjelpemidler.streams.serialization.serde
import kotlin.test.Test

class OppgaveTopologyTest {
    private val eksternOppgavehendelseSerde = jsonSerde<EksternOppgavehendelse>()
    private val internOppgavehendelseSerde = jsonSerde<InternOppgavehendelse>()

    private val driver = testTopology {
        oppgavehendelse()
    }

    private val inputTopic = driver.createInputTopic(
        Configuration.OPPGAVE_TOPIC,
        serde<Long>().serializer(),
        eksternOppgavehendelseSerde.serializer(),
    )
    private val outputTopic = driver.createOutputTopic(
        Configuration.KAFKA_RAPID_TOPIC,
        serde<String>().deserializer(),
        internOppgavehendelseSerde.deserializer(),
    )

    @Test
    fun `Skal transformere melding om oppgave og sende svaret videre på rapid`() {
        val oppgaveId = 123L

        inputTopic.pipeInput(oppgaveId, lagEksternOppgavehendelse(oppgaveId))

        val record = outputTopic.asSequence().single()
        record.key shouldBe oppgaveId.toString()

        val value = record.value
        value.oppgave.oppgaveId shouldBe oppgaveId.toString()
        value.oppgave.kategorisering.isTemaHjelpemidler shouldBe true
    }
}

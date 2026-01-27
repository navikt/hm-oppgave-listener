package no.nav.hjelpemidler.oppgave.listener.oppgave

import no.nav.hjelpemidler.kafka.KafkaEvent
import no.nav.hjelpemidler.kafka.KafkaMessage
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Hendelse
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.UtførtAv
import java.time.Instant
import java.util.UUID

@KafkaEvent(InternOppgavehendelse.EVENT_NAME)
data class InternOppgavehendelse(
    val hendelse: Hendelse,
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
    val opprettet: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID(),
) : KafkaMessage {
    constructor(oppgavehendelse: EksternOppgavehendelse) : this(
        hendelse = oppgavehendelse.hendelse,
        utførtAv = oppgavehendelse.utførtAv,
        oppgave = oppgavehendelse.oppgave,
    )

    companion object {
        const val EVENT_NAME = "hm-oppgave-event"
    }
}

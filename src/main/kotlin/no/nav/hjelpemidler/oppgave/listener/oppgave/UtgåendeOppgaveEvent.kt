package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import no.nav.hjelpemidler.kafka.KafkaEvent
import no.nav.hjelpemidler.kafka.KafkaMessage
import java.time.Instant
import java.util.UUID

@KafkaEvent(UtgåendeOppgaveEvent.EVENT_NAME)
data class UtgåendeOppgaveEvent(
    val hendelse: Hendelse,
    @param:JsonAlias("utfortAv")
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
    val opprettet: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID(),
) : KafkaMessage {
    constructor(innkommendeOppgaveEvent: InnkommendeOppgaveEvent) : this(
        hendelse = innkommendeOppgaveEvent.hendelse,
        utførtAv = innkommendeOppgaveEvent.utførtAv,
        oppgave = innkommendeOppgaveEvent.oppgave,
    )

    companion object {
        const val EVENT_NAME = "hm-oppgave-event"
    }
}

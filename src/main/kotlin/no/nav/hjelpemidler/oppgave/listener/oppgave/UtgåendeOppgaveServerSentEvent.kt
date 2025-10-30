package no.nav.hjelpemidler.oppgave.listener.oppgave

import no.nav.hjelpemidler.time.toInstant
import java.time.Instant

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

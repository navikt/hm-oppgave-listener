package no.nav.hjelpemidler.oppgave.listener.oppgave

import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse
import no.nav.hjelpemidler.time.toInstant
import java.time.Instant

data class InternOppgavehendelseSSE(
    val hendelsestype: EksternOppgavehendelse.Hendelse.Type,
    val tidspunkt: Instant,
    val oppgaveId: String,
    val versjon: Int,
) {
    constructor(oppgavehendelse: InternOppgavehendelse) : this(
        hendelsestype = oppgavehendelse.hendelse.hendelsestype,
        tidspunkt = oppgavehendelse.hendelse.tidspunkt.toInstant(),
        oppgaveId = oppgavehendelse.oppgave.oppgaveId,
        versjon = oppgavehendelse.oppgave.versjon,
    )
}

package no.nav.hjelpemidler.oppgave.listener.oppgave

import no.nav.hjelpemidler.domain.enhet.Enhetsnummer
import no.nav.hjelpemidler.domain.tilgang.NavIdent
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Hendelse
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave.Behandlingsperiode
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave.Bruker
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave.Kategorisering
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.Oppgave.Tilordning
import no.nav.hjelpemidler.oppgave.hendelse.EksternOppgavehendelse.UtførtAv
import java.time.LocalDate
import java.time.LocalDateTime

fun lagEksternOppgavehendelse(oppgaveId: Long = 1): EksternOppgavehendelse =
    EksternOppgavehendelse(
        hendelse = Hendelse(
            hendelsestype = Hendelse.Type.OPPGAVE_OPPRETTET,
            tidspunkt = LocalDateTime.of(2024, 9, 20, 10, 0, 0),
        ),
        utførtAv = UtførtAv(
            id = NavIdent("Z999999"),
            enhetsnummer = Enhetsnummer("9999"),
        ),
        Oppgave(
            oppgaveId = oppgaveId.toString(),
            versjon = 1,
            tilordning = Tilordning(
                enhetsnummer = Enhetsnummer("9999"),
                enhetsmappeId = "9999",
                navIdent = NavIdent("Z999999"),
            ),
            kategorisering = Kategorisering(
                tema = "HJE",
                oppgavetype = "BEH_SAK",
                behandlingstema = "",
                behandlingstype = "",
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

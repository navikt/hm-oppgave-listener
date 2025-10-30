package no.nav.hjelpemidler.oppgave.listener.oppgave

import no.nav.hjelpemidler.domain.enhet.Enhetsnummer
import java.time.LocalDate
import java.time.LocalDateTime

fun lagInnkommendeOppgaveEvent(oppgaveId: Long = 1): InnkommendeOppgaveEvent =
    InnkommendeOppgaveEvent(
        hendelse = Hendelse(
            hendelsestype = "OPPRETTET",
            tidspunkt = LocalDateTime.of(2024, 9, 20, 10, 0, 0),
        ),
        utførtAv = UtførtAv(
            ident = "Z999999",
            enhetsnummer = Enhetsnummer("9999"),
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

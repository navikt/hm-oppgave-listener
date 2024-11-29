package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import java.time.LocalDateTime

data class OppgaveEvent(
    val hendelse: Hendelse,
    @JsonAlias("utfortAv")
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
)

data class Hendelse(val hendelsestype: String, val tidspunkt: LocalDateTime)

data class UtførtAv(
    val navIdent: String,
    @JsonAlias("enhetsnr")
    val enhetsnummer: String?,
)

data class Oppgave(
    val oppgaveId: String,
    val versjon: Int,
    val tilordning: Tilordning?,
    val kategorisering: Kategorisering,
    val behandlingsperiode: Behandlingsperiode?,
    val bruker: Bruker?,
) {
    @get:JsonIgnore
    val erHjelpemiddel get() = kategorisering.tema == "HJE"
}

data class Tilordning(
    @JsonAlias("enhetsnr")
    val enhetsnummer: String?,
    val enhetsmappeId: String?,
    val navIdent: String?,
)

data class Kategorisering(
    val tema: String,
    val oppgavetype: String,
    val behandlingstema: String?,
    val behandlingstype: String?,
    val prioritet: Prioritet,
) {
    enum class Prioritet {
        HOY, NORMAL, LAV
    }
}

data class Behandlingsperiode(
    val aktiv: LocalDate,
    val frist: LocalDate,
)

data class Bruker(
    val ident: String,
    val identType: IdentType,
) {
    enum class IdentType {
        FOLKEREGISTERIDENT, NPID, ORGNR, SAMHANDLERNR
    }
}

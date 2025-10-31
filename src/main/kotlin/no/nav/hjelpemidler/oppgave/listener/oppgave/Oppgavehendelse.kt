package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.hjelpemidler.domain.enhet.Enhetsnummer
import java.time.LocalDate
import java.time.LocalDateTime

data class Hendelse(
    val hendelsestype: String,
    val tidspunkt: LocalDateTime,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
)

data class UtførtAv(
    /**
     * Nav-ident eller systemnavn.
     *
     * e.g. "A123456" eller "hm-oppgave-sink"
     */
    @param:JsonAlias("navIdent")
    val id: String,
    @param:JsonAlias("enhetsnr")
    val enhetsnummer: Enhetsnummer?,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
)

data class Oppgave(
    val oppgaveId: String,
    val versjon: Int,
    val tilordning: Tilordning?,
    val kategorisering: Kategorisering,
    val behandlingsperiode: Behandlingsperiode?,
    val bruker: Bruker?,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
) {
    val harTemaHjelpemidler @JsonIgnore get() = kategorisering.tema == "HJE"
}

data class Tilordning(
    @param:JsonAlias("enhetsnr")
    val enhetsnummer: String?,
    val enhetsmappeId: String?,
    val navIdent: String?,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
)

data class Kategorisering(
    val tema: String,
    val oppgavetype: String,
    val behandlingstema: String?,
    val behandlingstype: String?,
    val prioritet: Prioritet,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
) {
    enum class Prioritet {
        @JsonAlias("HOY")
        HØY,
        NORMAL,
        LAV,
    }
}

data class Behandlingsperiode(
    val aktiv: LocalDate,
    val frist: LocalDate?,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
)

data class Bruker(
    val ident: String,
    val identType: IdentType,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
) {
    enum class IdentType {
        FOLKEREGISTERIDENT,
        NPID,
        ORGNR,
        SAMHANDLERNR,
    }
}

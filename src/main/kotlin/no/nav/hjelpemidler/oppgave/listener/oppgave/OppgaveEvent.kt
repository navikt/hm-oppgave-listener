package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class OppgaveEvent(
    val hendelse: Hendelse,
    @JsonAlias("utfortAv")
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
) {
    @JsonAnySetter
    val andreFelter: Map<String, Any?> = mutableMapOf()
}

data class Hendelse(val hendelsestype: String, val tidspunkt: LocalDateTime) {
    @JsonAnySetter
    val andreFelter: Map<String, Any?> = mutableMapOf()
}

data class UtførtAv(
    val navIdent: String,
    @JsonAlias("enhetsnr")
    val enhetsnummer: String
) {
    @JsonAnySetter
    val andreFelter: Map<String, Any?> = mutableMapOf()
}

data class Oppgave(
    val oppgaveId: String,
    val versjon: Int,
    val tilordning: JsonNode,
    val kategorisering: Kategorisering,
    val behandlingsperiode: JsonNode,
    val bruker: JsonNode,
) {
    val erHjelpemiddel get() = kategorisering.tema == "HJE"
    @JsonAnySetter
    val andreFelter: Map<String, Any?> = mutableMapOf()
}

data class Kategorisering(
    val tema: String,
    val oppgavetype: String,
    val behandlingstema: String,
    val behandlingstype: String,
    val prioritet: Prioritet,
) {
    @JsonAnySetter
    val andreFelter: Map<String, Any?> = mutableMapOf()

    enum class Prioritet {
        HOY, NORMAL, LAV
    }
}

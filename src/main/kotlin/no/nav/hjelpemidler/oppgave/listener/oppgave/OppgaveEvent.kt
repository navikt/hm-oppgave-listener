package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.hjelpemidler.kafka.KafkaEvent
import no.nav.hjelpemidler.kafka.KafkaMessage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class InnkommendeOppgaveEvent(
    val hendelse: Hendelse,
    @param:JsonAlias("utfortAv")
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
)

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

data class Hendelse(val hendelsestype: String, val tidspunkt: LocalDateTime)

data class UtførtAv(
    val navIdent: String,
    @param:JsonAlias("enhetsnr")
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
    val harTemaHjelpemidler get() = kategorisering.tema == "HJE"
}

data class Tilordning(
    @param:JsonAlias("enhetsnr")
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
        @JsonAlias("HOY")
        HØY,
        NORMAL,
        LAV,
    }
}

data class Behandlingsperiode(
    val aktiv: LocalDate,
    val frist: LocalDate?,
)

data class Bruker(
    val ident: String,
    val identType: IdentType,
) {
    enum class IdentType {
        FOLKEREGISTERIDENT,
        NPID,
        ORGNR,
        SAMHANDLERNR,
    }
}

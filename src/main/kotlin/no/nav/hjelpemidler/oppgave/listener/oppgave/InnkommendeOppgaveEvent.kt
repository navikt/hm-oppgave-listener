package no.nav.hjelpemidler.oppgave.listener.oppgave

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

data class InnkommendeOppgaveEvent(
    val hendelse: Hendelse,
    @param:JsonAlias("utfortAv")
    val utførtAv: UtførtAv,
    val oppgave: Oppgave,
    @field:JsonAnyGetter
    @field:JsonAnySetter
    val andreFelter: MutableMap<String, Any?> = mutableMapOf(),
)

package no.nav.hjelpemidler.oppgave.listener.broker

import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow

interface MessageBroker : AutoCloseable {
    suspend fun publish(eventName: String, message: String)
    suspend fun subscribe(eventName: String): Flow<ServerSentEvent>
}

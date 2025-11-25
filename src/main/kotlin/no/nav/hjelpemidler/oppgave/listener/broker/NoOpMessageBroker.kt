package no.nav.hjelpemidler.oppgave.listener.broker

import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoOpMessageBroker : MessageBroker {
    override suspend fun publish(eventName: String, message: String) {
    }

    override suspend fun subscribe(eventName: String): Flow<ServerSentEvent> = emptyFlow()

    override fun close() {
    }
}

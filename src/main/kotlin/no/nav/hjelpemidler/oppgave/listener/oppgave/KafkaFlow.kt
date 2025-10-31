package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val log = KotlinLogging.logger {}

fun <K, V> Consumer<K, V>.asFlow(timeout: Duration = 1.seconds) = flow<ConsumerRecord<K, V>> {
    while (currentCoroutineContext().isActive) {
        poll(timeout.toJavaDuration()).forEach { emit(it) }
    }
}.onCompletion {
    if (it == null) {
        log.info { "Consumer flow completed successfully" }
    } else if (it !is CancellationException) {
        log.error(it) { "Consumer flow completed with error" }
    }
    try {
        close()
    } catch (e: Exception) {
        log.error(e) { "Consumer closed with error" }
    }
}.flowOn(Dispatchers.IO)

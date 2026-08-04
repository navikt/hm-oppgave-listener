package no.nav.hjelpemidler.oppgave.listener.oppgave

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.errors.WakeupException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val log = KotlinLogging.logger {}

fun <K, V> Consumer<K, V>.asFlow(
    timeout: Duration = 1.seconds,
): Flow<ConsumerRecord<K, V>> = flow {
    try {
        while (currentCoroutineContext().isActive) {
            val records = poll(timeout.toJavaDuration())
            for (record in records) {
                emit(record)
            }
        }
    } catch (e: WakeupException) {
        if (currentCoroutineContext().isActive) throw e
    } finally {
        try {
            close()
            log.info { "Consumer closed successfully" }
        } catch (e: Exception) {
            log.error(e) { "Consumer closed with error" }
        }
    }
}

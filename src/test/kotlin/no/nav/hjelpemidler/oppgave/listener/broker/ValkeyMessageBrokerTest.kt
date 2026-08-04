package no.nav.hjelpemidler.oppgave.listener.broker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@Disabled
class ValkeyMessageBrokerTest {
    private val broker: MessageBroker = ValkeyMessageBroker("broker")

    @Test
    suspend fun `Tester ut publish og subscribe`() {
        coroutineScope {
            val publishJob = launch(Dispatchers.Default) {
            }
            val subscribeJob = launch(Dispatchers.Default) {
                broker.subscribe("test").collect { println(it) }
            }
            launch(Dispatchers.Default) {
                delay(500.milliseconds)
                publishJob.cancelAndJoin()
                subscribeJob.cancelAndJoin()
            }
        }
    }
}

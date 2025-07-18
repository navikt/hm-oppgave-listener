package no.nav.hjelpemidler.oppgave.listener.broker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.Ignore

class ValkeyMessageBrokerTest {
    private val broker: MessageBroker = ValkeyMessageBroker("broker")

    @Test
    @Ignore
    fun `Tester ut publish og subscribe`() = runTest {
        val publishJob = launch(Dispatchers.Default) {
            while (true) {
                broker.publish("test", mapOf("timestamp" to Instant.now()))
                delay(100)
            }
        }
        val subscribeJob = launch(Dispatchers.Default) {
            broker.subscribe("test").collect { println(it) }
        }
        launch(Dispatchers.Default) {
            delay(500)
            publishJob.cancelAndJoin()
            subscribeJob.cancelAndJoin()
        }
    }
}

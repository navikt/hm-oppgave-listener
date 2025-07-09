package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.valkey.Jedis
import io.valkey.JedisPool
import io.valkey.JedisPubSub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import no.nav.hjelpemidler.serialization.jackson.toJson

interface MessagePublisher {
    fun publish(eventName: String, payload: Any): Job
}

interface MessageSubscriber {
    fun subscribe(eventName: String): Flow<String>
}

private val log = KotlinLogging.logger {}

class MessageBroker(
    private val jedisPool: JedisPool,
) : MessagePublisher, MessageSubscriber, AutoCloseable by jedisPool {
    constructor(valkeyConfiguration: ValkeyConfiguration) : this(
        JedisPool(
            valkeyConfiguration.host,
            valkeyConfiguration.port,
            valkeyConfiguration.username,
            valkeyConfiguration.password,
        )
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread("MessageBrokerShutdownHook") {
            override fun run() {
                try {
                    close()
                } catch (_: Exception) {
                }
            }
        })
    }

    override fun publish(eventName: String, payload: Any): Job =
        scope.launch {
            try {
                jedisPool.resource.use { jedis ->
                    jedis.publish(eventName, payload.toJson())
                }
            } catch (e: Exception) {
                throw RuntimeException("Feil under publish, channel: $eventName", e)
            }
        }

    override fun subscribe(eventName: String): Flow<String> =
        callbackFlow {
            val listener = object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    trySend(message)
                }
            }

            var jedis: Jedis? = null
            val job = launch(Dispatchers.IO) {
                jedis = jedisPool.resource
                try {
                    jedis?.subscribe(listener, eventName)
                } catch (e: Exception) {
                    close(e)
                }
            }

            awaitClose {
                listener.unsubscribe()
                job.cancel()
                jedis?.close()
            }
        }
}

package no.nav.hjelpemidler.oppgave.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.core.Closeable
import io.valkey.DefaultJedisClientConfig
import io.valkey.HostAndPort
import io.valkey.JedisPool
import io.valkey.JedisPubSub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import no.nav.hjelpemidler.serialization.jackson.toJson
import java.util.concurrent.Executors

interface MessagePublisher {
    fun publish(eventName: String, payload: Any): Job
}

interface MessageSubscriber {
    fun subscribe(eventName: String): Flow<String>
}

private val log = KotlinLogging.logger {}

class MessageBroker(
    private val jedisPool: JedisPool,
) : MessagePublisher, MessageSubscriber, Closeable {
    constructor(valkeyConfiguration: ValkeyConfiguration) : this(
        JedisPool(
            HostAndPort(valkeyConfiguration.host, valkeyConfiguration.port),
            DefaultJedisClientConfig.builder()
                .user(valkeyConfiguration.username)
                .password(valkeyConfiguration.password)
                .ssl(valkeyConfiguration.tls)
                .build(),
        ),
    )

    private val dispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)

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
                cancel("publish($eventName, payload) failed", e)
            }
        }

    override fun subscribe(eventName: String): Flow<String> =
        callbackFlow {
            val listener = object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    trySendBlocking(message)
                }
            }

            val jedis = jedisPool.resource
            launch(dispatcher) {
                try {
                    jedis.subscribe(listener, eventName)
                } catch (e: Exception) {
                    close(e)
                }
            }

            awaitClose {
                listener.unsubscribe()
                jedis.close()
            }
        }

    override fun close() {
        jedisPool.close()
        dispatcher.close()
        scope.cancel()
    }
}

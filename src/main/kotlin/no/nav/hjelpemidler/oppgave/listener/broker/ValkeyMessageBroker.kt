package no.nav.hjelpemidler.oppgave.listener.broker

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.core.Closeable
import io.valkey.DefaultJedisClientConfig
import io.valkey.HostAndPort
import io.valkey.JedisPool
import io.valkey.JedisPubSub
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import no.nav.hjelpemidler.serialization.jackson.toJson

interface MessageBroker : Closeable {
    fun publish(eventName: String, payload: Any): Job
    fun subscribe(eventName: String): Flow<String>
}

private val log = KotlinLogging.logger {}

class ValkeyMessageBroker private constructor(private val jedisPool: JedisPool) : MessageBroker {
    private val dispatcher = Dispatchers.IO + CoroutineName("ValkeyMessageBroker")
    private val scope = CoroutineScope(dispatcher)

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

    constructor(instanceName: String) : this(ValkeyConfiguration(instanceName))

    override fun publish(eventName: String, payload: Any): Job =
        scope.launch {
            try {
                jedisPool.resource.use { jedis ->
                    jedis.publish(eventName, payload.toJson())
                }
            } catch (e: Exception) {
                log.error(e) { "Publish failed, eventName: $eventName" }
            }
        }

    override fun subscribe(eventName: String): Flow<String> =
        callbackFlow {
            val listener = object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    trySend(message)
                }
            }

            val jedis = withContext(dispatcher) { jedisPool.resource }
            val job = launch(dispatcher) {
                try {
                    jedis.subscribe(listener, eventName)
                } catch (e: Exception) {
                    cancel(CancellationException("Subscribe failed, eventName: $eventName", e))
                }
            }

            awaitClose {
                log.info { "Subscription ended, eventName: $eventName" }
                if (listener.isSubscribed) listener.unsubscribe()
                jedis.close()
                job.cancel()
            }
        }.cancellable()

    override fun close() {
        log.info { "Shutting down ValkeyMessageBroker" }
        try {
            scope.cancel()
            jedisPool.close()
        } catch (_: Exception) {
        }
    }
}

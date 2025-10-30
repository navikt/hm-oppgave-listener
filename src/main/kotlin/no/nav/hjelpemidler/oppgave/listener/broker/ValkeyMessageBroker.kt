package no.nav.hjelpemidler.oppgave.listener.broker

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.core.Closeable
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.StaticCredentialsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import no.nav.hjelpemidler.configuration.ValkeyConfiguration
import java.lang.AutoCloseable

interface MessageBroker : Closeable {
    suspend fun subscribe(eventName: String): Flow<ServerSentEvent>
}

private val log = KotlinLogging.logger {}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class ValkeyMessageBroker private constructor(private val client: RedisClient) : MessageBroker, AutoCloseable {
    constructor(configuration: ValkeyConfiguration) : this(RedisClient.create(configuration.redisURI))
    constructor(instanceName: String) : this(ValkeyConfiguration(instanceName))

    private val commands = client.connectPubSub().reactive()

    override suspend fun subscribe(eventName: String): Flow<ServerSentEvent> {
        commands.subscribe(eventName).awaitSingleOrNull()
        return commands.observeChannels()
            .filter { it.channel == eventName }
            .map { ServerSentEvent(it.message, it.channel) }
            .asFlow()
    }

    override fun close() {
        log.info { "Shutting down ValkeyMessageBroker" }
        client.close()
    }
}

private val ValkeyConfiguration.redisURI: RedisURI
    get() = RedisURI.builder()
        .withHost(host)
        .withPort(port)
        .withSsl(tls)
        .withAuthentication(StaticCredentialsProvider(username, password?.toCharArray()))
        .build()

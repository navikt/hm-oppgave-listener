package no.nav.hjelpemidler.oppgave.listener.kafka

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.oppgave.listener.Configuration
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced

fun topology(block: StreamsBuilder.() -> Unit): Topology = StreamsBuilder().apply(block).build()

fun kafkaStreams(
    configuration: Map<String, String> = Configuration.kafkaStreamsConfiguration(),
    block: StreamsBuilder.() -> Unit,
): KafkaStreams = KafkaStreams(topology(block), configuration.toProperties())

infix fun <K, V> K.withValue(value: V): KeyValue<K, V> = KeyValue.pair(this, value)

inline fun <reified T> KStream<String, T>.toRapid() {
    if (Environment.current.isLocal) { // TODO: fjernes når meldinger skal videresendes til egen rapid (i første omgang vil vi bare logge ut det vi leser fra oppgave-topicet)
        to(Configuration.KAFKA_RAPID_TOPIC, Produced.with(stringSerde, jsonSerde<T>()))
    }
}

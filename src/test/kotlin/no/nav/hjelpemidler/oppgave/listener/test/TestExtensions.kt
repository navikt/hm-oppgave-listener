package no.nav.hjelpemidler.oppgave.listener.test

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.TestOutputTopic
import java.nio.file.Path

inline fun <reified T> JsonMapper.readValue(path: Path): T =
    readValue<T>(path.toFile())

fun <K, V> TestOutputTopic<K, V>.asSequence(): Sequence<KeyValue<K, V>> =
    generateSequence {
        when {
            isEmpty -> null
            else -> readKeyValue()
        }
    }

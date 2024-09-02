package no.nav.hjelpemidler.oppgave.listener.kafka

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import no.nav.hjelpemidler.oppgave.listener.jsonMapper
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

val stringSerde: Serde<String> = Serdes.String()

@Suppress("RemoveExplicitTypeArguments")
inline fun <reified T> jsonSerde(): Serde<T> {
    val typeReference = jacksonTypeRef<T>()
    return Serdes.serdeFrom(
        { _, data ->
            when (data) {
                null -> null
                else -> jsonMapper.writeValueAsBytes(data)
            }
        },
        { _, data ->
            when (data) {
                null -> null
                else -> jsonMapper.readValue<T>(data, typeReference)
            }
        },
    )
}

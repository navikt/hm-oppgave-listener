package no.nav.hjelpemidler.oppgave.listener.kafka

import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.serialization.jackson.jsonMapper
import kotlin.test.Test

class SerdesTest {
    private val serde = jsonSerde<Data>()
    private val topic = "test"
    private val data = Data("foobar")
    private val bytes = jsonMapper.writeValueAsBytes(data)

    @Test
    fun `Skal konvertere til JSON`() {
        serde.serializer().serialize(topic, data) shouldBe bytes
    }

    @Test
    fun `Skal konvertere fra JSON`() {
        serde.deserializer().deserialize(topic, bytes) shouldBe data
    }

    private data class Data(val value: String)
}

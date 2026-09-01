package no.nav.hjelpemidler.oppgave.listener.test

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TopologyTestDriver

fun testTopology(block: StreamsBuilder.() -> Unit): TopologyTestDriver =
    TopologyTestDriver(StreamsBuilder().apply(block).build())

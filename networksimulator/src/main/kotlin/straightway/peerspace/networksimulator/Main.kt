/*
 * Copyright 2016 github.com/straightway
 *
 *  Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package straightway.peerspace.networksimulator

import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.Network
import straightway.sim.net.Network as SimNetwork
import straightway.peerspace.net.Peer
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.InfrastructureImpl

import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerStubFactory
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.TransientDataChunkStore
import straightway.peerspace.net.impl.TransientPeerDirectory
import straightway.random.RandomChooser
import straightway.random.RandomSource
import straightway.sim.core.Simulator
import straightway.sim.net.AsyncSequentialTransmissionStream
import straightway.units.bit
import straightway.units.byte
import straightway.units.div
import straightway.units.get
import straightway.units.kilo
import straightway.units.mega
import straightway.units.milli
import straightway.units.second
import java.util.Random

private class MainClass(numberOfPeers: Int, randomSeed: Long) {

    val simulator = Simulator()

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY)

    private val simPeers = mutableMapOf<Id, SimNode>()

    private val peers = mutableMapOf<Id, Peer>()

    private val randomSource = RandomSource(Random(randomSeed))

    private fun createPeer(id: Id) {
        @Suppress("UNUSED_VARIABLE")
        val network = createPeerNetwork(id)
        peers[id] = PeerImpl(
                id,
                InfrastructureImpl(
                    TransientDataChunkStore(),
                    TransientPeerDirectory(),
                    network,
                    Configuration(),
                    RandomChooser(randomSource),
                    RandomChooser(randomSource),
                    ForwardStrategyImpl(),
                    simulator,
                    DataQueryHandlerImpl(id)))
    }

    private fun createPeerNetwork(peerId: Id): Network {
        val channelFactory = SimNode(
                peerId,
                peers,
                peers,
                simNet,
                { CHUNK_SIZE },
                uploadStream = AsyncSequentialTransmissionStream(
                        UPLOAD_BANDWIDTH,
                        simulator),
                downloadStream = AsyncSequentialTransmissionStream(
                        DOWNLOAD_BANDWIDTH,
                        simulator),
                simNodes = simPeers)
        val peerStubFactory = PeerStubFactory(channelFactory)
        return NetworkImpl(peerStubFactory, peerStubFactory)
    }

    init {
        for (i in 1..numberOfPeers)
            createPeer(Id("$i"))
    }

    private companion object {
        val LATENCY = 50[milli(second)]
        val UPLOAD_BANDWIDTH = 500[kilo(bit) / second]
        val DOWNLOAD_BANDWIDTH = 2[mega(bit) / second]
        val CHUNK_SIZE = 64[kilo(byte)]
    }
}

@Suppress("UNUSED_VARIABLE")
fun main(args: Array<String>) {
    println("Starting simulation")

    val mainClass = MainClass(numberOfPeers = 100, randomSeed = 1234L)
    mainClass.simulator.run()

    println("Simulation finished")
}
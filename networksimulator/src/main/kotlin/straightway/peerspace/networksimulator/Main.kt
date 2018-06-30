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

import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.withContext
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.sim.net.Network as SimNetwork
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QuerySource
import straightway.peerspace.net.impl.DataPushForwarderImpl
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.EpochKeyHasher
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.KnownPeersProviderImpl
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.PeerNetworkStub
import straightway.peerspace.net.impl.TimedDataQueryHandler
import straightway.peerspace.net.impl.TransientDataChunkStore
import straightway.peerspace.net.impl.TransientPeerDirectory
import straightway.peerspace.net.impl.UntimedDataQueryHandler
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
import straightway.utils.TimeProvider
import straightway.utils.toByteArray
import java.io.Serializable
import java.util.Random

private class MainClass(numberOfPeers: Int, randomSeed: Long) {

    private class SimHasher : Hasher {
        override fun getHash(obj: Serializable) = obj.hashCode().toByteArray()
    }

    val simulator = Simulator()

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY,
            offlineDetectionTime = OFFLINE_DETECTION_TIME)

    private val simPeers = mutableMapOf<Id, SimNode>()

    private val peers = mutableMapOf<Id, Peer>()

    private val randomSource = RandomSource(Random(randomSeed))

    private fun createPeer(id: Id) {

        @Suppress("MagicNumber")
        val hasher = EpochKeyHasher(SimHasher(), simulator, arrayOf(
                LongRange(0L, 86400000L), // epoch 0: 1 day
                LongRange(86400001L, 604800000L), // epoch 1: 1 week
                LongRange(604800001L, 2419200000L), // epoch 2: 4 weeks
                LongRange(2419200001L, 54021600000L), // epoch 3: 1 year
                LongRange(54021600001L, 540216000000L), // epoch 4: 10 years
                LongRange(540216000001L, Long.MAX_VALUE))) // epoch 5: more than 10 years

        val simNode = SimNode(
                id,
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

        peers[id] = withContext {
            bean { TransientDataChunkStore() as DataChunkStore }
            bean { DataQueryHandlerImpl(
                    UntimedDataQueryHandler(),
                    TimedDataQueryHandler()) as DataQueryHandler }
            bean { TransientPeerDirectory() as PeerDirectory }
            bean { NetworkImpl() as Network }
            factory {
                PeerNetworkStub(it.get("id")) as PushTarget
            }
            factory {
                PeerNetworkStub(it.get("id")) as QuerySource
            }
            factory {
                simNode.createChannel(it.get("id"))
            }
            bean { Configuration() }
            bean("knownPeerQueryChooser") { RandomChooser(randomSource) }
            bean("knownPeerAnswerChooser") { RandomChooser(randomSource) }
            bean { ForwardStrategyImpl(hasher) as ForwardStrategy }
            bean { simulator as TimeProvider }
            bean { DataPushForwarderImpl() }
            bean { KnownPeersProviderImpl() }
        }.apply {
            extraProperties["peerId"] = id.identifier
        } make {
            PeerImpl()
        }
    }

    /*
    @Suppress("UNUSED_PARAMETER")
    private fun createPeerNetwork(): Network {
        //val peerStubFactory = PeerStubFactory(channelFactory)
        return NetworkImpl()
    }*/

    init {
        for (i in 1..numberOfPeers)
            createPeer(Id("$i"))
    }

    private companion object {
        val LATENCY = 50[milli(second)]
        val OFFLINE_DETECTION_TIME = 5[second]
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
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

import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.koinutils.withContext
import straightway.peerspace.net.Configuration
import straightway.sim.net.Network as SimNetwork
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.net.impl.DataPushForwardTargetGetter
import straightway.peerspace.net.impl.DataPushTargetImpl
import straightway.peerspace.net.impl.DataQueryForwardTargetGetter
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.DataQuerySourceImpl
import straightway.peerspace.net.impl.EpochAnalyzerImpl
import straightway.peerspace.net.impl.EpochKeyHasher
import straightway.peerspace.net.impl.ForwardStateTrackerImpl
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.ForwarderImpl
import straightway.peerspace.net.impl.KnownPeersGetterImpl
import straightway.peerspace.net.impl.KnownPeersPushTargetImpl
import straightway.peerspace.net.impl.KnownPeersQuerySourceImpl
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerClientImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.PendingDataQueryTrackerImpl
import straightway.peerspace.net.impl.SeedPeerDirectory
import straightway.peerspace.net.impl.TimedDataQueryHandler
import straightway.peerspace.net.impl.TransientDataChunkStore
import straightway.peerspace.net.impl.TransientPeerDirectory
import straightway.peerspace.net.impl.UntimedDataQueryHandler
import straightway.peerspace.net.peer
import straightway.peerspace.net.pushForwardStateTracker
import straightway.peerspace.net.pushForwardTargetGetter
import straightway.peerspace.net.queryForwardStateTracker
import straightway.peerspace.net.queryForwardTargetGetter
import straightway.random.RandomChooser
import straightway.random.RandomSource
import straightway.sim.core.Simulator
import straightway.sim.net.AsyncSequentialTransmissionStream
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream
import straightway.units.bit
import straightway.units.byte
import straightway.units.div
import straightway.units.get
import straightway.units.ki
import straightway.units.me
import straightway.units.milli
import straightway.units.second
import straightway.utils.Event
import straightway.utils.toByteArray
import java.io.Serializable
import java.util.Random

@Suppress("LargeClass") // Temporary, must be cleaned up
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

    private val chunkSizeGetter = chunkSizeGetter { _ -> CHUNK_SIZE }

    @Suppress("MagicNumber")
    private fun createPeer(id: Id): Peer {
        lateinit var env: PeerComponent
        env = PeerComponent.createEnvironment(
                id,
                { Configuration() },
                { ForwardStrategyImpl() },
                { simulator },
                { DataQueryHandlerImpl() },
                { TimedDataQueryHandler() },
                { UntimedDataQueryHandler() },
                { RandomChooser(randomSource) },
                { RandomChooser(randomSource) },
                { SeedPeerDirectory(TransientPeerDirectory()) },
                { NetworkImpl() },
                { TransientDataChunkStore() },
                { PeerImpl() },
                { DataQueryForwardTargetGetter() },
                { DataPushForwardTargetGetter() },
                { PendingDataQueryTrackerImpl { timedDataQueryTimeout } },
                { PendingDataQueryTrackerImpl { untimedDataQueryTimeout } },
                { ForwardStateTrackerImpl() },
                { ForwarderImpl(env.queryForwardStateTracker, env.queryForwardTargetGetter) },
                { ForwardStateTrackerImpl() },
                { ForwarderImpl(env.pushForwardStateTracker, env.pushForwardTargetGetter) },
                { DataPushTargetImpl() },
                { DataQuerySourceImpl() },
                { KnownPeersPushTargetImpl() },
                { KnownPeersQuerySourceImpl() },
                { KnownPeersGetterImpl() },
                { chunkSizeGetter },
                { Event() },
                { Event() },
                { PeerClientImpl() },
                { EpochKeyHasher() },
                {
                    EpochAnalyzerImpl(arrayOf(
                            LongRange(0L, 86400000L), // epoch 0: 1 day
                            LongRange(86400001L, 604800000L), // epoch 1: 1 week
                            LongRange(604800001L, 2419200000L), // epoch 2: 4 weeks
                            LongRange(2419200001L, 54021600000L), // epoch 3: 1 year
                            LongRange(54021600001L, 540216000000L), // epoch 4: 10 years
                            LongRange(540216000001L, Long.MAX_VALUE))) // epoch 5: > 10 years
                },
                {
                    SimHasher()
                },
                { remotePeerId ->
                    val from = simPeers[id]!!
                    val to = simPeers[remotePeerId]!!
                    SimChannel(simNet, env.chunkSizeGetter, from, to)
                })

        return env.peer.also { peers[id] = it }
    }

    private fun createSimNode(parentPeer: Peer) =
            withContext {
                bean { parentPeer }
                bean("simNodes") { simPeers }
                bean { simNet as TransmissionRequestHandler }
                bean { chunkSizeGetter }
                bean("uploadStream") {
                    AsyncSequentialTransmissionStream(UPLOAD_BANDWIDTH, simulator)
                            as TransmissionStream
                }
                bean("downloadStream") {
                    AsyncSequentialTransmissionStream(DOWNLOAD_BANDWIDTH, simulator)
                            as TransmissionStream
                }
            }.apply {
                extraProperties["peerId"] = parentPeer.id
            } make {
                SimNode()
            }

    init {
        (1..numberOfPeers).map { Id("$it") }.forEach { createSimNode(createPeer(it)) }
    }

    private companion object {
        val LATENCY = 50[milli(second)]
        val OFFLINE_DETECTION_TIME = 5[second]
        val UPLOAD_BANDWIDTH = 500[ki(bit) / second]
        val DOWNLOAD_BANDWIDTH = 2[me(bit) / second]
        val CHUNK_SIZE = 64[ki(byte)]
    }
}

@Suppress("UNUSED_VARIABLE")
fun main(args: Array<String>) {
    KoinLoggingDisabler().use {
        println("Starting simulation")

        val mainClass = MainClass(numberOfPeers = 100, randomSeed = 1234L)
        mainClass.simulator.run()

        println("Simulation finished")
    }
}
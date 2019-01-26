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
package straightway.peerspace.integrationtest

import straightway.koinutils.withContext
import straightway.koinutils.Bean.get
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.PeerClient
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.impl.DataPushForwardTargetGetter
import straightway.peerspace.net.impl.DataPushTargetImpl
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.ForwardStateTrackerImpl
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.PendingDataQueryTrackerImpl
import straightway.peerspace.net.impl.DataQueryForwardTargetGetter
import straightway.peerspace.net.impl.DataQuerySourceImpl
import straightway.peerspace.net.impl.EpochAnalyzerImpl
import straightway.peerspace.net.impl.EpochKeyHasher
import straightway.peerspace.net.impl.ForwarderImpl
import straightway.peerspace.net.impl.KnownPeersGetterImpl
import straightway.peerspace.net.impl.KnownPeersPushTargetImpl
import straightway.peerspace.net.impl.KnownPeersQuerySourceImpl
import straightway.peerspace.net.impl.PeerClientImpl
import straightway.peerspace.net.impl.SeedPeerDirectory
import straightway.peerspace.net.impl.TimedDataQueryHandler
import straightway.peerspace.net.impl.TransientDataChunkStore
import straightway.peerspace.net.impl.TransientPeerDirectory
import straightway.peerspace.net.impl.UntimedDataQueryHandler
import straightway.peerspace.net.pushForwardStateTracker
import straightway.peerspace.net.pushForwardTargetGetter
import straightway.peerspace.net.queryForwardStateTracker
import straightway.peerspace.net.queryForwardTargetGetter
import straightway.peerspace.networksimulator.SimChannel
import straightway.peerspace.networksimulator.SimNode
import straightway.random.RandomChooser
import straightway.random.RandomSource
import straightway.sim.core.Simulator
import straightway.sim.net.AsyncSequentialTransmissionStream
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream
import straightway.units.bit
import straightway.units.div
import straightway.units.get
import straightway.units.mi
import straightway.units.milli
import straightway.units.second
import straightway.utils.Event
import straightway.sim.net.Network as SimNetwork
import straightway.utils.toByteArray
import java.util.Arrays
import java.util.Random

class SinglePeerEnvironment(
        private val randomSource: RandomSource = RandomSource(Random(1234L)),
        val simulator: Simulator = Simulator(),
        peerId: Id = Id("peerId"),
        private val simNodes: MutableMap<Any, SimNode> = mutableMapOf(),
        val peerFactory: () -> Peer = {
            PeerImpl()
        },
        val dataPushTargetFactory: () -> DataPushTarget = {
            DataPushTargetImpl()
        },
        val knownPeersPushTargetFactory: () -> KnownPeersPushTarget = {
            KnownPeersPushTargetImpl()
        },
        val dataQuerySourceFactory: () -> DataQuerySource = {
            DataQuerySourceImpl()
        },
        val knownPeersQuerySourceFactory: () -> KnownPeersQuerySource = {
            KnownPeersQuerySourceImpl()
        },
        forwardStrategyFactory: () -> ForwardStrategy = {
            ForwardStrategyImpl()
        },
        keyHasherFactory: () -> KeyHasher = {
            EpochKeyHasher()
        }
) {
    private companion object {
        val latency = 50.0[milli(second)]
        val offlineDetectionTime = 30.0[second]
        val uploadBandwidth = 1[mi(bit / second)]
        val downloadBandwidth = 2[mi(bit / second)]
    }

    private val simNetwork =
            SimNetwork(simulator, simulator, latency, offlineDetectionTime)

    val peer get() = get<Peer>()
    val client get() = get<PeerClient>()
    val node get() = simNodes[peer.id]!!

    inline fun <reified T> get(name: String = "") = koin.get<T>(name)

    fun addRemotePeer(remotePeer: Peer) {
        val simNode = createSimNode(remotePeer)
        addRemoteNode(simNode)
    }

    fun addRemoteNode(remoteNode: SimNode) {
        simNodes[remoteNode.id] = remoteNode
    }

    fun addKnownPeer(peerId: Id) =
        get<PeerDirectory>().add(peerId)

    fun addData(chunk: DataChunk) {
        val dataStore = get<DataChunkStore>()
        dataStore.store(chunk)
    }

    val koin: PeerComponent by lazy {
        PeerComponent.createEnvironment(
                peerId,
                { Configuration() },
                { forwardStrategyFactory() },
                { simulator },
                { DataQueryHandlerImpl() },
                { TimedDataQueryHandler() },
                { UntimedDataQueryHandler() },
                { RandomChooser(randomSource) },
                { RandomChooser(randomSource) },
                { SeedPeerDirectory(TransientPeerDirectory()) },
                { NetworkImpl() },
                { TransientDataChunkStore() },
                { peerFactory().apply { addRemotePeer(this) } },
                { DataQueryForwardTargetGetter() },
                { DataPushForwardTargetGetter() },
                { PendingDataQueryTrackerImpl { timedDataQueryTimeout } },
                { PendingDataQueryTrackerImpl { untimedDataQueryTimeout } },
                { ForwardStateTrackerImpl() },
                { ForwarderImpl(koin.queryForwardStateTracker, koin.queryForwardTargetGetter) },
                { ForwardStateTrackerImpl() },
                { ForwarderImpl(koin.pushForwardStateTracker, koin.pushForwardTargetGetter) },
                { dataPushTargetFactory() },
                { dataQuerySourceFactory() },
                { knownPeersPushTargetFactory() },
                { knownPeersQuerySourceFactory() },
                { KnownPeersGetterImpl() },
                { Event() },
                { Event() },
                { PeerClientImpl() },
                { keyHasherFactory() },
                {
                    EpochAnalyzerImpl(arrayOf(
                        LongRange(0L, 86400000L), // epoch 0: 1 day
                        LongRange(86400001L, 604800000L), // epoch 1: 1 week
                        LongRange(604800001L, 2419200000L), // epoch 2: 4 weeks
                        LongRange(2419200001L, 54021600000L), // epoch 3: 1 year
                        LongRange(54021600001L, 540216000000L), // epoch 4: 10 years
                        LongRange(540216000001L, Long.MAX_VALUE))) // epoch 5: more than 10 years
                },
                {
                object : Hasher {
                    override val algorithm = "FastTestHasher"
                    override val hashBits = Int.SIZE_BITS
                    override fun getHash(data: ByteArray) = Arrays.hashCode(data).toByteArray()
                }
            },
                { remoteNodeId ->
                val from = simNodes[peerId]!!
                val to = simNodes[remoteNodeId]!!
                SimChannel(simNetwork, from, to)
            })
    }

    private fun createSimNode(parentPeer: Peer) =
            withContext {
                bean { parentPeer }
                bean("simNodes") { simNodes }
                bean { simNetwork as TransmissionRequestHandler }
                bean("uploadStream") {
                    AsyncSequentialTransmissionStream(uploadBandwidth, simulator)
                            as TransmissionStream
                }
                bean("downloadStream") {
                    AsyncSequentialTransmissionStream(downloadBandwidth, simulator)
                            as TransmissionStream
                }
            }.apply {
                extraProperties["peerId"] = parentPeer.id
            } make {
                SimNode()
            }
}

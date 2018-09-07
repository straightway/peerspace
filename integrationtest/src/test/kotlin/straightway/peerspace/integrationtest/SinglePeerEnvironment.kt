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

import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.koinutils.Bean.get
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.Channel
import straightway.peerspace.net.ChunkSizeGetter
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.PeerClient
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.net.impl.DataPushForwarder
import straightway.peerspace.net.impl.DataPushTargetImpl
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.ForwardStateTrackerImpl
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.PendingDataQueryTrackerImpl
import straightway.peerspace.net.impl.DataQueryForwarder
import straightway.peerspace.net.impl.DataQuerySourceImpl
import straightway.peerspace.net.impl.EpochAnalyzerImpl
import straightway.peerspace.net.impl.EpochKeyHasher
import straightway.peerspace.net.impl.KnownPeersGetterImpl
import straightway.peerspace.net.impl.KnownPeersPushTargetImpl
import straightway.peerspace.net.impl.KnownPeersQuerySourceImpl
import straightway.peerspace.net.impl.PeerClientImpl
import straightway.peerspace.net.impl.TimedDataQueryHandler
import straightway.peerspace.net.impl.TransientDataChunkStore
import straightway.peerspace.net.impl.TransientPeerDirectory
import straightway.peerspace.net.impl.UntimedDataQueryHandler
import straightway.peerspace.networksimulator.SimChannel
import straightway.peerspace.networksimulator.SimNode
import straightway.random.Chooser
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
import straightway.sim.net.Network as SimNetwork
import straightway.utils.TimeProvider
import straightway.utils.toByteArray
import java.io.Serializable
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
        val latency = 50[milli(second)]
        val offlineDetectionTime = 30[second]
        val uploadBandwidth = 1[me(bit / second)]
        val downloadBandwidth = 2[me(bit / second)]
    }

    private val simNetwork =
            SimNetwork(simulator, simulator, latency, offlineDetectionTime)

    private val chunkSizeGetter: ChunkSizeGetter = { _: Serializable -> 64[ki(byte)] }

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

    val koin = withContext {
        bean {
            peerFactory().apply { addRemotePeer(this) }
        }
        bean("localDataPushTarget") { dataPushTargetFactory() }
        bean("localDataQuerySource") { dataQuerySourceFactory() }
        bean("localKnownPeersPushTarget") { knownPeersPushTargetFactory() }
        bean("localKnownPeersQuerySource") { knownPeersQuerySourceFactory() }
        bean {
            KnownPeersGetterImpl() as KnownPeersGetter
        }
        bean {
            Configuration()
        }
        bean {
            forwardStrategyFactory()
        }
        bean {
            simulator as TimeProvider
        }
        bean("dataQueryHandler") {
            DataQueryHandlerImpl() as DataQueryHandler
        }
        bean("timedDataQueryHandler") {
            TimedDataQueryHandler() as DataQueryHandler
        }
        bean("untimedDataQueryHandler") {
            UntimedDataQueryHandler() as DataQueryHandler
        }
        bean("knownPeerQueryChooser") {
            RandomChooser(randomSource) as Chooser
        }
        bean("knownPeerAnswerChooser") {
            RandomChooser(randomSource) as Chooser
        }
        bean {
            TransientPeerDirectory() as PeerDirectory
        }
        bean {
            NetworkImpl() as Network
        }
        bean {
            TransientDataChunkStore() as DataChunkStore
        }
        bean("queryForwarder") {
            DataQueryForwarder() as Forwarder<DataQuery>
        }
        bean("pendingTimedQueryTracker") {
            PendingDataQueryTrackerImpl { timedDataQueryTimeout } as PendingDataQueryTracker
        }
        bean("pendingUntimedQueryTracker") {
            PendingDataQueryTrackerImpl { untimedDataQueryTimeout } as PendingDataQueryTracker
        }
        bean("queryForwardTracker") {
            ForwardStateTrackerImpl<DataQuery>(get("queryForwarder"))
                    as ForwardStateTracker<DataQuery>
        }
        bean("pushForwarder") {
            DataPushForwarder() as Forwarder<DataChunk>
        }
        bean("pushForwardTracker") {
            ForwardStateTrackerImpl<DataChunk>(get("pushForwarder"))
                    as ForwardStateTracker<DataChunk>
        }
        bean {
            chunkSizeGetter { _ -> 64[ki(byte)] }
        }
        bean("localDeliveryEvent") {
            Event<Transmittable>()
        }
        bean("knownPeersReceivedEvent") {
            Event<KnownPeers>()
        }
        bean {
            PeerClientImpl() as PeerClient
        }
        bean {
            keyHasherFactory()
        }
        bean {
            EpochAnalyzerImpl(arrayOf(
                    LongRange(0L, 86400000L), // epoch 0: 1 day
                    LongRange(86400001L, 604800000L), // epoch 1: 1 week
                    LongRange(604800001L, 2419200000L), // epoch 2: 4 weeks
                    LongRange(2419200001L, 54021600000L), // epoch 3: 1 year
                    LongRange(54021600001L, 540216000000L), // epoch 4: 10 years
                    LongRange(540216000001L, Long.MAX_VALUE))) // epoch 5: more than 10 years
             as EpochAnalyzer
        }
        bean {
            object : Hasher {
                override fun getHash(obj: Serializable) = obj.hashCode().toByteArray()
            } as Hasher
        }
        factory {
            val from = simNodes[peerId]!!
            val to = simNodes[it["id"]]!!
            SimChannel(simNetwork, chunkSizeGetter, from, to) as Channel
        }
    }.apply {
        extraProperties["peerId"] = peerId.identifier
    } make {
        KoinModuleComponent()
    }

    private fun createSimNode(parentPeer: Peer) =
            withContext {
                bean { parentPeer }
                bean("simNodes") { simNodes }
                bean { simNetwork as TransmissionRequestHandler }
                bean { chunkSizeGetter }
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
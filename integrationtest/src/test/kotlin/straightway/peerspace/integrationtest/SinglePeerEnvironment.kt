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
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Channel
import straightway.peerspace.net.ChunkSizeGetter
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.net.impl.DataPushForwarderImpl
import straightway.peerspace.net.impl.DataQueryHandlerImpl
import straightway.peerspace.net.impl.ForwardStateTrackerImpl
import straightway.peerspace.net.impl.ForwardStrategyImpl
import straightway.peerspace.net.impl.KnownPeersProviderImpl
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.net.impl.PeerNetworkStub
import straightway.peerspace.net.impl.PendingDataQueryTrackerImpl
import straightway.peerspace.net.impl.DataQueryForwarder
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
import straightway.sim.net.Network as SimNetwork
import straightway.utils.TimeProvider
import java.io.Serializable
import java.util.Random

class SinglePeerEnvironment(
        private val randomSource: RandomSource = RandomSource(Random(1234L)),
        val simulator: Simulator = Simulator(),
        peerId: Id = Id("peerId"),
        forwardStrategyFactory: () -> ForwardStrategy = { ForwardStrategyImpl() }
) {
    private companion object {
        val latency = 50[milli(second)]
        val offlineDetectionTime = 30[second]
        val uploadBandwidth = 1[me(bit / second)]
        val downloadBandwidth = 2[me(bit / second)]
    }

    private val _simNodes = mutableMapOf<Id, SimNode>()

    private val simNetwork =
            SimNetwork(simulator, simulator, latency, offlineDetectionTime)

    private val chunkSizeGetter: ChunkSizeGetter = { _: Serializable -> 64[ki(byte)] }

    val simNodes: Map<Id, SimNode> get() = _simNodes
    val peer get() = koin.get<Peer>()

    fun addRemotePeer(remotePeer: Peer) {
        val simNode = createSimNode(remotePeer)
        _simNodes[remotePeer.id] = simNode
    }

    private val koin = withContext {
        bean {
            PeerImpl().apply {
                addRemotePeer(this)
            } as Peer
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
        bean {
            DataPushForwarderImpl() as DataPushForwarder
        }
        bean {
            KnownPeersProviderImpl()
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
            DataQueryForwarder() as Forwarder<DataQueryRequest, DataQueryRequest>
        }
        bean("pendingTimedQueryTracker") {
            PendingDataQueryTrackerImpl { timedDataQueryTimeout } as PendingDataQueryTracker
        }
        bean("pendingUntimedQueryTracker") {
            PendingDataQueryTrackerImpl { untimedDataQueryTimeout } as PendingDataQueryTracker
        }
        bean("queryForwardTracker") {
            ForwardStateTrackerImpl<DataQueryRequest, DataQueryRequest>(get("queryForwarder"))
                    as ForwardStateTracker<DataQueryRequest, DataQueryRequest>
        }
        bean("pushForwarder") {
            straightway.peerspace.net.impl.DataPushForwarder() as Forwarder<DataPushRequest, Key>
        }
        bean("pushForwardTracker") {
            ForwardStateTrackerImpl<DataPushRequest, Key>(get("pushForwarder"))
                    as ForwardStateTracker<DataPushRequest, Key>
        }
        bean {
            chunkSizeGetter { _ -> 64[ki(byte)] }
        }
        factory {
            PeerNetworkStub(it["id"]) as DataPushTarget
        }
        factory {
            PeerNetworkStub(it["id"]) as DataQuerySource
        }
        factory {
            val from = _simNodes[peerId]!!
            val to = _simNodes[it["id"]]!!
            SimChannel(simNetwork, chunkSizeGetter, from, to) as Channel
        }
    }.apply {
        extraProperties["peerId"] = peerId.identifier
    } make {
        KoinModuleComponent()
    }

    fun createSimNode(parentPeer: Peer) =
            withContext {
                bean { parentPeer as DataPushTarget }
                bean { parentPeer as DataQuerySource }
                bean("simNodes") { _simNodes }
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
                extraProperties["peerId"] = parentPeer.id.identifier
            } make {
                SimNode()
            }
}
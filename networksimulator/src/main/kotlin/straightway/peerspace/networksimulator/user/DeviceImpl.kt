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
package straightway.peerspace.networksimulator.user

import straightway.koinutils.Bean.get
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerClient
import straightway.peerspace.net.PeerComponent
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
import straightway.peerspace.net.pushForwardStateTracker
import straightway.peerspace.net.pushForwardTargetGetter
import straightway.peerspace.net.queryForwardStateTracker
import straightway.peerspace.net.queryForwardTargetGetter
import straightway.peerspace.networksimulator.SimChannel
import straightway.peerspace.networksimulator.SimNode
import straightway.peerspace.networksimulator.profile.dsl.DeviceProfile
import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.random.RandomChooser
import straightway.sim.net.AsyncSequentialTransmissionStream
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream
import straightway.utils.Event
import straightway.utils.TimeProvider
import straightway.utils.toByteArray
import java.util.Arrays

/**
 * Default implementation of the Device interface.
 */
class DeviceImpl(override val id: Id, override val usage: DeviceUsageProfile) :
        Device, KoinModuleComponent by KoinModuleComponent() {

    // region Component references

    private val transmissionRequestHandler: TransmissionRequestHandler by inject()
    private val simNodes: MutableMap<Any, SimNode> by inject("simNodes")
    private val timeProvider: TimeProvider by inject()
    private val randomSource: Iterator<Byte> by inject("randomSource")

    // endregion

    // region Device implementation

    override var isOnline: Boolean
        get() = node.isOnline
        set(value) {
            node.isOnline = value
        }

    override val peerClient: PeerClient

    // endregion

    // region Private

    private fun createNode(device: DeviceProfile, peer: Peer): SimNode {
        return withContext {
            bean { peer }
            bean("simNodes") { simNodes }
            bean { transmissionRequestHandler }
            bean("uploadStream") {
                AsyncSequentialTransmissionStream(device.uploadBandwidth.value, timeProvider)
                        as TransmissionStream
            }
            bean("downloadStream") {
                AsyncSequentialTransmissionStream(
                        device.downloadBandwidth.value, timeProvider) as TransmissionStream
            }
        }.apply {
            extraProperties["peerId"] = peer.id
        } make {
            SimNode()
        }
    }

    @Suppress("MagicNumber", "LongMethod")
    private fun createPeerEnvironment(): PeerComponent {
        lateinit var environment: PeerComponent
        environment = PeerComponent.createEnvironment(
                id,
                { Configuration() },
                { ForwardStrategyImpl() },
                { timeProvider },
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
                {
                    ForwarderImpl(
                            environment.queryForwardStateTracker,
                            environment.queryForwardTargetGetter)
                },
                { ForwardStateTrackerImpl() },
                {
                    ForwarderImpl(
                            environment.pushForwardStateTracker,
                            environment.pushForwardTargetGetter)
                },
                { DataPushTargetImpl() },
                { DataQuerySourceImpl() },
                { KnownPeersPushTargetImpl() },
                { KnownPeersQuerySourceImpl() },
                { KnownPeersGetterImpl() },
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
                    object : Hasher {
                        override val algorithm = "FastTestHasher"
                        override val hashBits = Int.SIZE_BITS
                        override fun getHash(data: ByteArray) = Arrays.hashCode(data).toByteArray()
                    }
                },
                { remoteNodeId ->
                    val from = simNodes[id]!!
                    val to = simNodes[remoteNodeId]!!
                    SimChannel(transmissionRequestHandler, from, to)
                })

        return environment
    }

    private val node: SimNode

    init {
        val peerEnv = createPeerEnvironment()
        peerClient = peerEnv.get()
        node = createNode(usage.device.value, peerEnv.get())
        node.isOnline = false
    }

    // endregion
}
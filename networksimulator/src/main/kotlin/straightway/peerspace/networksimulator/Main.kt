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
import straightway.peerspace.networksimulator.profile.officeWorker
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

    val simulator = Simulator()

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY,
            offlineDetectionTime = OFFLINE_DETECTION_TIME)

    private val simNodes = mutableMapOf<Id, SimNode>()

    private val randomSource = RandomSource(Random(randomSeed))

    private val chunkSizeGetter = chunkSizeGetter { _ -> CHUNK_SIZE }

    val users = (1..numberOfPeers).map { _ ->
            withContext {
                bean("simNodes") { simNodes }
                bean { _ -> officeWorker }
                bean { _ -> RandomSource(Random(1234L)) }
                bean { _ -> simulator }
                bean { _ -> chunkSizeGetter }
                bean { _ -> simNet }
            } make {
                User()
            }
        }

    private companion object {
        val LATENCY = 50[milli(second)]
        val OFFLINE_DETECTION_TIME = 5[second]
        val CHUNK_SIZE = 64[ki(byte)]
    }
}

@Suppress("UNUSED_VARIABLE")
fun main(args: Array<String>) {
    KoinLoggingDisabler().use {
        println("Starting simulation")

        val mainClass = MainClass(numberOfPeers = 100, randomSeed = 1234L)
        println("Created ${mainClass.users.size} users")
        mainClass.simulator.run()

        println("Simulation finished")
    }
}
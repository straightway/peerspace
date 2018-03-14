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

import straightway.peerspace.Infrastructure
import straightway.peerspace.data.Id
import straightway.sim.net.Network as SimNetwork
import straightway.peerspace.net.Peer
import straightway.peerspace.net.impl.NetworkImpl
import straightway.peerspace.net.impl.PeerFactoryImpl
import straightway.sim.core.Simulator
import straightway.sim.net.AsyncSequentialTransmissionStream
import straightway.units.bit
import straightway.units.div
import straightway.units.get
import straightway.units.kilo
import straightway.units.mega
import straightway.units.milli
import straightway.units.second

private class MainClass(numberOfPeers: Int) {

    val simulator = Simulator()

    @Suppress("UNUSED")
    private val simulatedNetwork = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY)

    private val simPeers = mutableMapOf<Id, SimPeer>()

    @Suppress("UNUSED")
    private val peers = mutableMapOf<Id, Peer>()

    init {
        for (i in 1..numberOfPeers)
            createPeer("$i")
    }

    private fun createPeer(id: String) {
        @Suppress("UNUSED_VARIABLE")
        val infrastructure = createPeerInfrastructure(id)
        // Create new peer
    }

    private fun createPeerInfrastructure(peerId: String): Infrastructure {
        return Infrastructure {
            network = NetworkImpl(this)
            peerFactory = PeerFactoryImpl(this)
            channelFactory = SimPeer(
                    peerId,
                    uploadStream = AsyncSequentialTransmissionStream(
                            UPLOAD_BANDWIDTH,
                            simulator),
                    downloadStream = AsyncSequentialTransmissionStream(
                            DOWNLOAD_BANDWIDTH,
                            simulator),
                    createdInstances = simPeers)
        }
    }

    private companion object {
        val LATENCY = 50[milli(second)]
        val UPLOAD_BANDWIDTH = 500[kilo(bit) / second]
        val DOWNLOAD_BANDWIDTH = 2[mega(bit) / second]
    }
}

@Suppress("UNUSED_VARIABLE")
fun main(args: Array<String>) {
    println("Starting simulation")

    val mainClass = MainClass(numberOfPeers = 100)
    mainClass.simulator.run()

    println("Simulation finished")
}
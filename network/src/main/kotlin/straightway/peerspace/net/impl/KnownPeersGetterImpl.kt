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

package straightway.peerspace.net.impl

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersQuery
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Transmission
import straightway.random.Chooser

/**
 * Default implementation of the KnownPeersGetter interface.
 */
class KnownPeersGetterImpl :
        KnownPeersGetter,
        KoinModuleComponent by KoinModuleComponent() {

    private val id: Id by property("peerId") { Id(it) }
    private val network: Network by inject()
    private val configuration: Configuration by inject()
    private val knownPeerQueryChooser: Chooser by inject("knownPeerQueryChooser")
    private val peerDirectory: PeerDirectory by inject()

    override fun refreshKnownPeers() {
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }
        network.executePendingRequests()
    }

    private fun queryForKnownPeers(targetPeerId: Id) =
            network.scheduleTransmission(
                    Transmission(targetPeerId, KnownPeersQuery()))

    private val peersToQueryForOtherKnownPeers get() =
        knownPeerQueryChooser choosePeers configuration.maxPeersToQueryForKnownPeers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(peerDirectory.allKnownPeersIds.toList(), number)
}
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

import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.Id
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersQuery
import straightway.peerspace.net.Request
import straightway.peerspace.net.configuration
import straightway.peerspace.net.knownPeerQueryChooser
import straightway.peerspace.net.network
import straightway.peerspace.net.peerDirectory
import straightway.random.Chooser

/**
 * Default implementation of the KnownPeersGetter interface.
 */
class KnownPeersGetterImpl :
        KnownPeersGetter,
        KoinModuleComponent by KoinModuleComponent() {

    override fun refreshKnownPeers() {
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }
        network.executePendingRequests()
    }

    private fun queryForKnownPeers(targetPeerId: Id) =
            network.scheduleTransmission(Request(targetPeerId, KnownPeersQuery()))

    private val peersToQueryForOtherKnownPeers get() =
        knownPeerQueryChooser choosePeers configuration.maxPeersToQueryForKnownPeers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(peerDirectory.allKnownPeersIds.toList(), number)
}
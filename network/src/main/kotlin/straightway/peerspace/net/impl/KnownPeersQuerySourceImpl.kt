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
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersQuery
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Request
import straightway.random.Chooser

/**
 * Default implementation of the KnownPeersQuerySource interface.
 */
class KnownPeersQuerySourceImpl :
        KnownPeersQuerySource,
        KoinModuleComponent by KoinModuleComponent() {

    private val configuration: Configuration by inject()
    private val peerDirectory: PeerDirectory by inject()
    private val network: Network by inject()
    private val knownPeerAnswerChooser: Chooser by inject("knownPeerAnswerChooser")

    override fun queryKnownPeers(request: Request<KnownPeersQuery>) {
        pushKnownPeersTo(request.remotePeerId)
        network.executePendingRequests()
    }

    private fun pushKnownPeersTo(targetPeerId: Id) =
            network.scheduleTransmission(
                    Request(targetPeerId, knownPeersAnswerRequest))

    private val knownPeersAnswerRequest
        get() = KnownPeers(knownPeersQueryAnswer)

    private val knownPeersQueryAnswer
        get() = knownPeerAnswerChooser choosePeers configuration.maxKnownPeersAnswers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(allKnownPeersIds, number)

    private val allKnownPeersIds
        get() = peerDirectory.allKnownPeersIds.toList()
}
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

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.Peer
import straightway.random.Chooser
import straightway.utils.TimeProvider

/**
 * Test environment for testing the PeerImpl class.
 */
@Suppress("ComplexInterface")
interface PeerTestEnvironment : InfrastructureProvider {
    val peerId: Id
    val knownPeersIds: List<Id>
    val unknownPeerIds: List<Id>
    var configuration: Configuration
    val localChunks: List<Chunk>
    val knownPeers: List<Peer>
    val unknownPeers: List<Peer>
    var knownPeerQueryChooser: Chooser
    var knownPeerAnswerChooser: Chooser
    var timeProvider: TimeProvider
    val peer: PeerImpl
    var forwardStrategy: ForwardStrategy
    var dataQueryHandler: DataQueryHandler
    var dataPushForwarder: DataPushForwarder
    var knownPeersProvider: KnownPeersProvider

    fun fixed(): PeerTestEnvironment
}

fun PeerTestEnvironment.getPeer(id: Id) =
        knownPeers.find { it.id == id } ?: unknownPeers.find { it.id == id }!!

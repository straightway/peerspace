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
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.random.Chooser

@Suppress("ComplexInterface")
interface PeerTestEnvironment {
    val peerId: Id
    val knownPeersIds: List<Id>
    val unknownPeerIds: List<Id>
    var configuration: Configuration
    val localChunks: List<Chunk>
    val knownPeers: List<Peer>
    val unknownPeers: List<Peer>
    val network: Network
    val peerDirectory: PeerDirectory
    var knownPeerQueryChooser: Chooser
    var knownPeerAnswerChooser: Chooser
    val chunkDataStore: DataChunkStore
    val sut: PeerImpl
    fun getPeer(id: Id): Peer
}
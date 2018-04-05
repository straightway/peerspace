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

import straightway.peerspace.data.Id
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.utils.Chooser

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl(
        override val id: Id,
        private val dataChunkStore: DataChunkStore,
        private val peerDirectory: PeerDirectory,
        private val network: Network,
        private val configuration: Configuration/*,
        private val knownPeerQueryChoose: Chooser<Id>*/
) : Peer {

    fun refreshKnownPeers() =
            peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }

    override fun push(request: PushRequest) =
            dataChunkStore.store(request.chunk)

    override fun query(request: QueryRequest) {
        val originator by lazy { network.getPushTarget(request.originatorId) }
        val queryResult = dataChunkStore.query(request)
        queryResult.forEach { originator.push(PushRequest(it)) }
    }

    override fun toString() = "PeerImpl(${id.identifier})"

    private val peersToQueryForOtherKnownPeers: Iterable<Id>
        get() {
            val allKnownPeersIds = peerDirectory.allKnownPeersIds
            return allKnownPeersIds.take(configuration.maxPeersToQueryForKnownPeers)
        }

    private fun queryForKnownPeers(it: Id) {
        val peer = network.getQuerySource(it)
        peer.query(QueryRequest(id, Administrative.KnownPeers))
    }
}
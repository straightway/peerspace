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
import straightway.peerspace.data.Key
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.utils.serializeToByteArray

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl(
        override val id: Id,
        private val infrastructure: Infrastructure
) : Peer {

    fun refreshKnownPeers() =
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }

    override fun push(request: PushRequest) {
        infrastructure.dataChunkStore.store(request.chunk)
        forwardPushRequest(request)
    }

    override fun query(request: QueryRequest) {
        when (request.id) {
            Administrative.KnownPeers.id -> pushBackKnownPeersTo(request.originatorId)
            else -> dataQueryHandler.handleDataQuery(request)
        }
    }

    override fun toString() = "PeerImpl(${id.identifier})"

    private fun forwardPushRequest(request: PushRequest) {
        infrastructure.forwardStrategy.getPushForwardPeerIdsFor(request.chunk.key).forEach {
            getPushTargetFor(it).push(request)
        }

        dataQueryHandler.notifyDataArrived(request)
    }

    private fun pushBackKnownPeersTo(originatorId: Id) =
            getPushTargetFor(originatorId).push(
                    PushRequest(Chunk(knownPeersChunkKey, serializedKnownPeersQueryAnswer)))

    private fun getPushTargetFor(id: Id) = infrastructure.network.getPushTarget(id)

    private val knownPeersChunkKey = Key(Administrative.KnownPeers.id)

    private val serializedKnownPeersQueryAnswer get() =
        knownPeersQueryAnswer.serializeToByteArray()

    private val knownPeersQueryAnswer get() =
        infrastructure.knownPeerAnswerChooser.chooseFrom(
                    allKnownPeersIds,
                    infrastructure.configuration.maxKnownPeersAnswers)

    private val peersToQueryForOtherKnownPeers get() =
        infrastructure.knownPeerQueryChooser.chooseFrom(
                allKnownPeersIds,
                infrastructure.configuration.maxPeersToQueryForKnownPeers)

    private val allKnownPeersIds get() = infrastructure.peerDirectory.allKnownPeersIds.toList()

    private fun queryForKnownPeers(it: Id) {
        val peer = infrastructure.network.getQuerySource(it)
        peer.query(QueryRequest(id, Administrative.KnownPeers))
    }

    private val dataQueryHandler = DataQueryHandler(id, infrastructure)
}
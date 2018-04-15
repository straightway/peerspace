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
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isMatching
import straightway.random.Chooser
import straightway.units.toDuration
import straightway.utils.TimeProvider
import straightway.utils.serializeToByteArray
import java.time.LocalDateTime

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl(
        override val id: Id,
        private val dataChunkStore: DataChunkStore,
        private val peerDirectory: PeerDirectory,
        private val network: Network,
        private val configuration: Configuration,
        private val knownPeerQueryChooser: Chooser,
        private val knownPeerAnswerChooser: Chooser,
        private val forwardStrategy: ForwardStrategy,
        private val timeProvider: TimeProvider
) : Peer {

    fun refreshKnownPeers() =
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }

    override fun push(request: PushRequest) {
        dataChunkStore.store(request.chunk)
        forwardPushRequest(request)
    }

    override fun query(request: QueryRequest) {
        when (request.id) {
            Administrative.KnownPeers.id -> pushBackKnownPeersTo(request.originatorId)
            else -> handleDataQuery(request)
        }
    }

    override fun toString() = "PeerImpl(${id.identifier})"

    private fun forwardPushRequest(request: PushRequest) {
        forwardStrategy.getPushForwardPeerIdsFor(request.chunk.key).forEach {
            getPushTargetFor(it).push(request)
        }

        pendingQueries.filter { it.query.isMatching(request.chunk.key) }.forEach {
            getPushTargetFor(it.query.originatorId).push(request)
        }
    }

    private fun handleDataQuery(request: QueryRequest) {
        _pendingQueries += PendingQuery(request, timeProvider.currentTime)

        pushBackDataQueryResult(request)

        val forwardedRequest = request.copy(originatorId = id)
        forwardStrategy.getQueryForwardPeerIdsFor(request).forEach {
            getQuerySourceFor(it).query(forwardedRequest)
        }
    }

    private fun pushBackDataQueryResult(request: QueryRequest) {
        val originator by lazy { request.pushTarget }
        val queryResult = dataChunkStore.query(request)
        queryResult.forEach { originator.push(PushRequest(it)) }
    }

    private fun pushBackKnownPeersTo(originatorId: Id) =
            getPushTargetFor(originatorId).push(
                    PushRequest(Chunk(knownPeersChunkKey, serializedKnownPeersQueryAnswer)))

    private val QueryRequest.pushTarget get() = getPushTargetFor(originatorId)

    private fun getPushTargetFor(id: Id) = network.getPushTarget(id)

    private fun getQuerySourceFor(id: Id) = network.getQuerySource(id)

    private val knownPeersChunkKey = Key(Administrative.KnownPeers.id)

    private val serializedKnownPeersQueryAnswer get() =
        knownPeersQueryAnswer.serializeToByteArray()

    private val knownPeersQueryAnswer get() =
            knownPeerAnswerChooser.chooseFrom(
                    allKnownPeersIds,
                    configuration.maxKnownPeersAnswers)

    private val peersToQueryForOtherKnownPeers get() =
            knownPeerQueryChooser.chooseFrom(
                allKnownPeersIds,
                configuration.maxPeersToQueryForKnownPeers)

    private val allKnownPeersIds get() = peerDirectory.allKnownPeersIds.toList()

    private fun queryForKnownPeers(it: Id) {
        val peer = network.getQuerySource(it)
        peer.query(QueryRequest(id, Administrative.KnownPeers))
    }

    data class PendingQuery(val query: QueryRequest, val receiveTime: LocalDateTime)

    private val pendingQueries: List<PendingQuery> get() {
        removeOldPendingQueries()
        return _pendingQueries
    }

    private fun removeOldPendingQueries() {
        val tooOldTime =
                timeProvider.currentTime - configuration.untimedDataQueryTimeout.toDuration()
        _pendingQueries.removeAll { it.receiveTime < tooOldTime }
    }

    private val _pendingQueries = mutableListOf<PendingQuery>()
}
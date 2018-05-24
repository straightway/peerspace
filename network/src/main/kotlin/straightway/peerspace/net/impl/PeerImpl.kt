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
@file:Suppress("ForbiddenComment")
package straightway.peerspace.net.impl

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.random.Chooser
import straightway.utils.serializeToByteArray

// TODO:
// * Avoid routing loops:
// ** Don't push the same chunk twice to the same peer (within a certain time)
// * Collect known peers while receiving requests
// * Push to other peers if some peers returned by the strategy are not reachable.

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl(
        override val id: Id,
        override val infrastructure: Infrastructure
) : Peer, InfrastructureProvider {

    fun refreshKnownPeers() =
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }

    override fun push(request: PushRequest, resultListener: TransmissionResultListener) {
        dataChunkStore.store(request.chunk)
        dataPushForwarder.forward(request)
        resultListener.notifySuccess()
    }

    override fun query(request: QueryRequest, resultListener: TransmissionResultListener) {
        when (request.id) {
            Administrative.KnownPeers.id -> pushBackKnownPeersTo(request.originatorId)
            else -> dataQueryHandler.handle(request)
        }
        resultListener.notifySuccess()
    }

    override fun toString() = "PeerImpl(${id.identifier})"

    private fun pushBackKnownPeersTo(originatorId: Id) =
            originatorId.asPushTarget.push(knownPeersAnswerRequest)

    private val knownPeersAnswerRequest
        get() = PushRequest(id, Chunk(knownPeersChunkKey, serializedKnownPeersQueryAnswer))

    private val serializedKnownPeersQueryAnswer
        get() = knownPeersQueryAnswer.serializeToByteArray()

    private val knownPeersQueryAnswer
        get() = knownPeerAnswerChooser choosePeers configuration.maxKnownPeersAnswers

    private val peersToQueryForOtherKnownPeers
        get() = knownPeerQueryChooser choosePeers configuration.maxPeersToQueryForKnownPeers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(allKnownPeersIds, number)

    private val allKnownPeersIds
        get() = peerDirectory.allKnownPeersIds.toList()

    private fun queryForKnownPeers(peerId: Id) =
        peerId.asQuerySource.query(QueryRequest(id, Administrative.KnownPeers))

    private val Id.asQuerySource
        get() = getQuerySourceFor(this)

    private val Id.asPushTarget
        get() = getPushTargetFor(this)

    private companion object {
        val knownPeersChunkKey = Key(Administrative.KnownPeers.id)
    }
}
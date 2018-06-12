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
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.inject
import straightway.peerspace.koinutils.property
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.TransmissionResultListener

/**
 * Push data to a target peer.
 */
class DataPushForwarderImpl : DataPushForwarder, KoinModuleComponent by KoinModuleComponent() {

    override fun forward(push: PushRequest) {
        push.forwardPeers.forEach { push pushOnTo it }
        dataQueryHandler.notifyChunkForwarded(push.chunk.key)
    }

    val forwardStates get() = _forwardStates

    private val id: Id by property("peerId") { Id(it) }
    private val dataQueryHandler: DataQueryHandler by inject()
    private val forwardStrategy: ForwardStrategy by inject()
    private val network: Network by inject()

    private val PushRequest.forwardPeers
        get() = forwardPeersFromStrategies.toSet().filter { it != originatorId }

    private infix fun PushRequest.pushOnTo(receiverId: Id) {
        setTransmissionPendingTo(receiverId)
        val target = network.getPushTarget(receiverId)
        val request = PushRequest(id, chunk)
        target.push(request, ResultListener(request, receiverId))
    }

    private fun PushRequest.setTransmissionPendingTo(receiverId: Id) {
        val oldState = _forwardStates[chunk.key] ?: ForwardState()
        _forwardStates += Pair(
                chunk.key,
                ForwardState(
                        successful = oldState.successful - receiverId,
                        failed = oldState.failed - receiverId,
                        pending = oldState.pending - receiverId + receiverId))
    }

    private val PushRequest.forwardPeersFromStrategies
        get() = pushForwardPeerIds + queryForwardPeerIds

    private val PushRequest.pushForwardPeerIds
        get() = forwardStrategy.getPushForwardPeerIdsFor(chunk.key, ForwardState())

    private val PushRequest.queryForwardPeerIds: Iterable<Id>
        get() = dataQueryHandler.getForwardPeerIdsFor(chunk.key)

    private inner class ResultListener(
            private val push: PushRequest,
            private val receiverId: Id
    ) : TransmissionResultListener {

        override fun notifySuccess() {
            updateTransmissionState(success = receiverId)
        }

        override fun notifyFailure() {
            val newState = updateTransmissionState(fail = receiverId)
            val rePushPeers = forwardStrategy.getPushForwardPeerIdsFor(chunkKey, newState)
            rePushPeers.forEach { push pushOnTo it }
        }

        private val chunkKey get() = push.chunk.key

        private fun updateTransmissionState(success: Id? = null, fail: Id? = null ) =
                _forwardStates[chunkKey]!!.updated(success, fail).also {
                    _forwardStates += Pair(chunkKey, it)
                }

        private fun ForwardState.updated(success: Id? = null, fail: Id? = null) =
                ForwardState(
                        pending = pending.filter { it != success && it != fail },
                        successful = successful + success.list,
                        failed = failed + fail.list)

        private val Id?.list get() = if (this == null) listOf() else listOf(this)
    }

    private var _forwardStates = mapOf<Key, ForwardState>()
}
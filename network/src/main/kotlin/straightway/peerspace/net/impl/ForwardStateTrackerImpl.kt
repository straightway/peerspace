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
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Network
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener

/**
 * Forward data items keeping track of transmission success or failure and re-asking
 * the strategy on failure.
 */
class ForwardStateTrackerImpl<TItem : Transmittable>(
        private val forwarder: Forwarder<TItem>) :
        ForwardStateTracker<TItem>,
        KoinModuleComponent by KoinModuleComponent() {

    private val network: Network by inject()

    override fun forward(request: Request<TItem>) {
        val forwardState = getStateFor(request.content.id)
        val forwardPeerIds = forwarder.getForwardPeerIdsFor(request, forwardState)
        forwardPeerIds.forEach { peerId -> request forwardToPeer peerId }
    }

    override fun getStateFor(itemKey: Any) = states.getOrDefault(itemKey, ForwardState())

    override val forwardStates get() = states

    private infix fun Request<TItem>.forwardToPeer(targetPeerId: Id) {
        setPending(content.id, targetPeerId)
        content.forwardTo(targetPeerId, object : TransmissionResultListener {
            override fun notifySuccess() { setSuccess(this@forwardToPeer, targetPeerId) }
            override fun notifyFailure() { setFailed(this@forwardToPeer, targetPeerId) }
        })
    }

    private fun TItem.forwardTo(
            target: Id,
            transmissionResultListener: TransmissionResultListener
    ) = network.scheduleTransmission(
            Request.createDynamically(target, this),
            transmissionResultListener)

    private fun setPending(itemKey: Any, targetPeerId: Id) {
        val newState = getStateFor(itemKey).setPending(targetPeerId)
        states += Pair(itemKey, newState)
    }

    private fun setSuccess(item: Request<TItem>, targetPeerId: Id) {
        val itemKey = item.content.id
        val newState = getStateFor(itemKey).setSuccess(targetPeerId)
        states += Pair(itemKey, newState)
        clearFinishedTransmissionFor(itemKey)
    }

    private fun setFailed(item: Request<TItem>, targetPeerId: Id) {
        markStateFailedFor(item, targetPeerId)
        forward(item)
        network.executePendingRequests()
        clearFinishedTransmissionFor(item.content.id)
    }

    fun markStateFailedFor(item: Request<TItem>, targetPeerId: Id) {
        val newState = getStateFor(item.content.id).setFailed(targetPeerId)
        states += Pair(item.content.id, newState)
    }

    private fun clearFinishedTransmissionFor(itemKey: Any) {
        val pendingTransmissions = getStateFor(itemKey).pending
        if (pendingTransmissions.isEmpty())
            states -= itemKey
    }

    private var states = mapOf<Any, ForwardState>()
}
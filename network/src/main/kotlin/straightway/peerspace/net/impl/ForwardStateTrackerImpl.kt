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
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.TransmissionResultListener

/**
 * Forward data items keeping track of transmission success or failure and re-asking
 * the strategy on failure.
 */
class ForwardStateTrackerImpl<TItem, TKey>(private val forwarder: Forwarder<TItem, TKey>) :
        ForwardStateTracker<TItem, TKey> {

    override fun forward(item: TItem) {
        val forwardState = getStateFor(item.key)
        val forwardPeerIds = forwarder.getForwardPeerIdsFor(item, forwardState)
        forwardPeerIds.forEach { peerId -> item forwardToPeer peerId }
    }

    override fun getStateFor(itemKey: TKey) = states.getOrDefault(itemKey, ForwardState())

    override val forwardStates get() = states

    private infix fun TItem.forwardToPeer(peerId: Id) {
        setPending(key, peerId)
        forwarder.forwardTo(peerId, this, object : TransmissionResultListener {
            override fun notifySuccess() { setSuccess(key, peerId) }
            override fun notifyFailure() { setFailed(this@forwardToPeer, peerId) }
        })
    }

    private fun setPending(itemKey: TKey, targetPeerId: Id) {
        val newState = getStateFor(itemKey).setPending(targetPeerId)
        states += Pair(itemKey, newState)
    }

    private fun setSuccess(itemKey: TKey, targetPeerId: Id) {
        val newState = getStateFor(itemKey).setSuccess(targetPeerId)
        states += Pair(itemKey, newState)
        clearFinishedTransmissionFor(itemKey)
    }

    private fun setFailed(item: TItem, targetPeerId: Id) {
        val newState = getStateFor(item.key).setFailed(targetPeerId)
        states += Pair(item.key, newState)
        forward(item)
        clearFinishedTransmissionFor(item.key)
    }

    private val TItem.key get() = forwarder.getKeyFor(this)

    private fun clearFinishedTransmissionFor(itemKey: TKey) {
        val pendingTransmissions = getStateFor(itemKey).pending
        if (pendingTransmissions.isEmpty())
            states -= itemKey
    }

    private var states = mapOf<TKey, ForwardState>()
}
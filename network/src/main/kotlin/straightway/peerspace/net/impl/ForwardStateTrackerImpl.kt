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
import straightway.koinutils.Property.property
import straightway.peerspace.data.Id
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Network
import straightway.peerspace.net.Transmission
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.Transmittable

/**
 * Forward data items keeping track of transmission success or failure and re-asking
 * the strategy on failure.
 */
class ForwardStateTrackerImpl<TItem : Transmittable>(
        private val forwarder: Forwarder<TItem>) :
        ForwardStateTracker<TItem>,
        KoinModuleComponent by KoinModuleComponent() {

    private val peerId: Id by property("peerId") { Id(it) }
    private val network: Network by inject()

    override fun forward(item: TItem) {
        val forwardState = getStateFor(item.identification)
        val forwardPeerIds = forwarder.getForwardPeerIdsFor(item, forwardState)
        forwardPeerIds.forEach { peerId -> item forwardToPeer peerId }
    }

    override fun getStateFor(itemKey: Any) = states.getOrDefault(itemKey, ForwardState())

    override val forwardStates get() = states

    private infix fun TItem.forwardToPeer(peerId: Id) {
        setPending(identification, peerId)
        forwardTo(peerId, object : TransmissionResultListener {
            override fun notifySuccess() { setSuccess(identification, peerId) }
            override fun notifyFailure() { setFailed(this@forwardToPeer, peerId) }
        })
    }

    private fun TItem.forwardTo(
            target: Id,
            transmissionResultListener: TransmissionResultListener
    ) = network.scheduleTransmission(
            Transmission(target, withOriginator(peerId)),
            transmissionResultListener)

    private fun setPending(itemKey: Any, targetPeerId: Id) {
        val newState = getStateFor(itemKey).setPending(targetPeerId)
        states += Pair(itemKey, newState)
    }

    private fun setSuccess(itemKey: Any, targetPeerId: Id) {
        val newState = getStateFor(itemKey).setSuccess(targetPeerId)
        states += Pair(itemKey, newState)
        clearFinishedTransmissionFor(itemKey)
    }

    private fun setFailed(item: TItem, targetPeerId: Id) {
        val newState = getStateFor(item.identification).setFailed(targetPeerId)
        states += Pair(item.identification, newState)
        forward(item)
        clearFinishedTransmissionFor(item.identification)
    }

    private fun clearFinishedTransmissionFor(itemKey: Any) {
        val pendingTransmissions = getStateFor(itemKey).pending
        if (pendingTransmissions.isEmpty())
            states -= itemKey
    }

    private var states = mapOf<Any, ForwardState>()
}
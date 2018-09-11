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
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.ForwardTargetGetter
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.Network
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.utils.Event
import straightway.utils.handleOnce

/**
 * Forward a given request to other peers.
 */
class ForwarderImpl(
        private val tracker: ForwardStateTracker,
        private val forwardTargetGetter: ForwardTargetGetter
) : Forwarder, KoinModuleComponent by KoinModuleComponent() {

    private val network: Network by inject()
    private val knownPeersGetter: KnownPeersGetter by inject()
    private val knownPeersReceivedEvent: Event<KnownPeers> by inject("knownPeersReceivedEvent")
    private val configuration: Configuration by inject()

    override fun forward(request: Request<*>) { request.forward() }

    private fun Request<*>.retryForwardAfterKnownPeersAreRefreshed(
            pendingRetries: Int = configuration.forwardRetries
    ) {
        if (0 < pendingRetries) {
            knownPeersReceivedEvent.handleOnce {
                if (forward(pendingRetries - 1)) network.executePendingRequests()
            }
            knownPeersGetter.refreshKnownPeers()
        }
    }

    private fun Request<*>.forward(retries: Int = configuration.forwardRetries) =
            with(forwardPeerIds) {
                if (any()) {
                    forwardTo(this); true
                } else {
                    retryForwardAfterKnownPeersAreRefreshed(retries); false
                }
            }

    private val Request<*>.forwardPeerIds: List<Id> get() =
        forwardTargetGetter.getForwardPeerIdsFor(
                this,
                tracker.get(content.id)).toList()

    private infix fun Request<*>.forwardToPeer(targetPeerId: Id) {
        tracker[content.id] = tracker[content.id].setPending(targetPeerId)
        content.forwardTo(targetPeerId, object : TransmissionResultListener {
            override fun notifySuccess() {
                tracker[content.id] = tracker[content.id].setSuccess(targetPeerId)
                tracker.clearFinishedTransmissionFor(content.id)
            }
            override fun notifyFailure() {
                tracker[content.id] = tracker[content.id].setFailed(targetPeerId)
                forward(this@forwardToPeer)
                network.executePendingRequests()
                tracker.clearFinishedTransmissionFor(content.id)
            }
        })
    }

    private fun Request<*>.forwardTo(receiverIds: List<Id>) =
            receiverIds.forEach { this forwardToPeer it }

    private fun Transmittable.forwardTo(
            target: Id,
            transmissionResultListener: TransmissionResultListener
    ) = network.scheduleTransmission(
            Request.createDynamically(target, this),
            transmissionResultListener)
}
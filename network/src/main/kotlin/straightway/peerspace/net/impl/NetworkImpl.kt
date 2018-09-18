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
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.createChannelTo
import straightway.peerspace.net.localDeliveryEvent
import straightway.peerspace.net.localPeerId
import straightway.peerspace.net.peerDirectory

/**
 * Productive implementation of the Network interface.
 */
class NetworkImpl : Network, PeerComponent by PeerComponent() {

    override fun scheduleTransmission(
            transmission: Request<*>,
            resultListener: TransmissionResultListener
    ) {
        val pendingTransmission = pendingTransmissions.getOrPut(transmission.key) {
            PendingTransmission(transmission)
        }
        pendingTransmission.transmissionResultListeners += resultListener
    }

    override fun executePendingRequests() {
        val actionsToExecute = pendingTransmissions.values.toList()
        pendingTransmissions.clear()
        actionsToExecute.forEach { it.execute() }
    }

    private val pendingTransmissions = mutableMapOf<Pair<Id, Any>, PendingTransmission>()
    private val Request<*>.channel get() = createChannelTo(remotePeerId)
    private val Request<*>.key get() = kotlin.Pair(remotePeerId, content.id)

    private inner class PendingTransmission(val transmission: Request<*>) {

        var transmissionResultListeners = listOf<TransmissionResultListener>()

        fun execute() =
                if (transmission.isLocal) deliverLocally()
                else deliverViaNetwork()

        private val Request<*>.isLocal get() = remotePeerId == localPeerId

        private fun deliverLocally() {
            distributingListener.notifySuccess()
            localDeliveryEvent(transmission.content)
        }

        private fun deliverViaNetwork() =
            transmission.channel.transmit(transmission.content, distributingListener)

        private fun forAllListeners(action: TransmissionResultListener.() -> Unit) =
                transmissionResultListeners.forEach { it.action() }

        private val distributingListener = object : TransmissionResultListener {
            override fun notifySuccess() = forAllListeners { notifySuccess() }
            override fun notifyFailure() {
                peerDirectory.setUnreachable(transmission.remotePeerId)
                forAllListeners { notifyFailure() }
            }
        }
    }
}
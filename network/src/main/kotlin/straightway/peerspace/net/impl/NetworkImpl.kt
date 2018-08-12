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

import straightway.koinutils.Bean.get
import straightway.peerspace.data.Id
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Network
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.Transmission
import straightway.peerspace.net.TransmissionResultListener

/**
 * Productive implementation of the Network interface.
 */
class NetworkImpl : Network, KoinModuleComponent by KoinModuleComponent() {

    override fun scheduleTransmission(
            transmission: Transmission,
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

    private inner class PendingTransmission(val transmission: Transmission) {
        var transmissionResultListeners = listOf<TransmissionResultListener>()
        fun execute() = transmission.channel.transmit(transmission.content, distributingListener)
        private fun forAllListeners(action: TransmissionResultListener.() -> Unit) =
                transmissionResultListeners.forEach { it.action() }
        private val distributingListener = object : TransmissionResultListener {
            override fun notifySuccess() = forAllListeners { notifySuccess() }
            override fun notifyFailure() = forAllListeners { notifyFailure() }
        }
    }

    private val pendingTransmissions = mutableMapOf<Pair<Id, Any>, PendingTransmission>()

    private val Transmission.channel get() = get<Channel> { mapOf("id" to receiverId) }
}
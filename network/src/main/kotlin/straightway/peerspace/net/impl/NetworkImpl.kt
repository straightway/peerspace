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
import straightway.peerspace.data.Key
import straightway.peerspace.net.Network
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.TransmissionResultListener

/**
 * Productive implementation of the Network interface.
 */
class NetworkImpl : Network, KoinModuleComponent by KoinModuleComponent() {

    private data class PendingPush(
            val receiver: DataPushTarget,
            val request: DataPushRequest
    ) {
        val transmissionResultListeners = mutableListOf<TransmissionResultListener>()
        fun execute() {
            val listeners = transmissionResultListeners.toList()
            receiver.push(request, object : TransmissionResultListener {
                override fun notifySuccess() = listeners.forEach { it.notifySuccess() }
                override fun notifyFailure() = listeners.forEach { it.notifyFailure() }
            })
        }
    }

    private val pendingPushes = mutableMapOf<Pair<Id, Key>, PendingPush>()

    private inner class DelayedPushTarget(
            val id: Id,
            val wrapped: DataPushTarget
    ) : DataPushTarget {
        override fun push(
                request: DataPushRequest,
                resultListener: TransmissionResultListener
        ) {
            val pendingPush = pendingPushes.getOrPut(Pair(id, request.chunk.key)) {
                PendingPush(wrapped, request)
            }
            pendingPush.transmissionResultListeners.add(resultListener)
        }
    }

    override fun getPushTarget(id: Id): DataPushTarget =
            DelayedPushTarget(id, get("networkDataPushTarget") { mapOf("id" to id) })

    override fun getQuerySource(id: Id): DataQuerySource =
            get("networkDataQuerySource") { mapOf("id" to id) }

    override fun getKnownPeersPushTarget(id: Id): KnownPeersPushTarget {
        TODO("not implemented")
    }

    override fun getKnownPeersQuerySource(id: Id): KnownPeersQuerySource {
        TODO("not implemented")
    }

    override fun executePendingRequests() {
        val actionsToExecute = pendingPushes.values.toList()
        pendingPushes.clear()
        actionsToExecute.forEach { it.execute() }
    }
}
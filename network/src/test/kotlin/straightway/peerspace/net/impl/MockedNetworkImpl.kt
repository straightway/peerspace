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
import straightway.peerspace.net.Peer
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.handle

class MockedNetworkImpl(
        private val localPeerId: Id,
        private val transmissionResultListeners: MutableList<TransmissionRecord>,
        private val peers: () -> Collection<Peer>
) : MockedNetwork {
    override fun scheduleTransmission(
            transmission: Request<*>,
            resultListener: TransmissionResultListener
    ) {
        pendingTransmissions.add {
            if (transmission.remotePeerId in unreachablePeers)
                resultListener.notifyFailure()
            else {
                val request = transmission.content
                transmissionResultListeners.add(TransmissionRecord(request, resultListener))
                val peer = peers().find { it.id == transmission.remotePeerId }!!
                peer.handle(Request.createDynamically(localPeerId, request))
                resultListener.notifySuccess()
            }
        }
    }

    override fun executePendingRequests() {
        if (isSuspended)
            suspendedTransmissions.addAll(pendingTransmissions)
        else pendingTransmissions.forEach { it() }
        pendingTransmissions.clear()
    }

    override var isSuspended: Boolean
            get() = _isSuspended
            set(value) {
                if (value != isSuspended) {
                    _isSuspended = value
                    if (!_isSuspended) {
                        suspendedTransmissions.forEach { transmission -> transmission() }
                        suspendedTransmissions.clear()
                    }
                }
            }

    override fun setUnreachable(peerId: Id) { unreachablePeers.add(peerId) }

    private val unreachablePeers = mutableSetOf<Id>()
    private var _isSuspended = false
    private val suspendedTransmissions = mutableListOf<() -> Unit>()
    private val pendingTransmissions = mutableListOf<() -> Unit>()
}
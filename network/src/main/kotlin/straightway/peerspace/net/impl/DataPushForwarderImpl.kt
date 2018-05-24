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
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.InfrastructureReceiver
import straightway.peerspace.net.PushRequest

class DataPushForwarderImpl(
        private val id: Id
) : DataPushForwarder, InfrastructureProvider, InfrastructureReceiver {

    override lateinit var infrastructure: Infrastructure

    override fun forward(push: PushRequest) {
        push.forwardPeers.forEach { push pushOnTo it }
        dataQueryHandler.notifyChunkForwarded(push.chunk.key)
    }

    private val PushRequest.forwardPeers
        get() = forwardPeersFromStrategies.toSet().filter { it != originatorId }

    private infix fun PushRequest.pushOnTo(receiverId: Id) =
            getPushTargetFor(receiverId).push(PushRequest(id, chunk))

    private val PushRequest.forwardPeersFromStrategies
        get() = pushForwardPeerIds + queryForwardPeerIds

    private val PushRequest.pushForwardPeerIds
        get() = forwardStrategy.getPushForwardPeerIdsFor(chunk.key, ForwardState())

    private val PushRequest.queryForwardPeerIds: Iterable<Id>
        get() = dataQueryHandler.getForwardPeerIdsFor(chunk.key)

}
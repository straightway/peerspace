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
@file:Suppress("ForbiddenComment")

package straightway.peerspace.net.impl

import straightway.peerspace.data.Id
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Factory
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener

/**
 * Implementation of a network stub for a peer.
 *
 * This network stub cares for transmitting data to the physical
 * network node the peer runs on.
 */
class PeerNetworkStub(
        override val id: Id,
        var channelFactory: Factory<Channel> // TODO: Get via Koin
) : Peer {

    override fun push(
            request: PushRequest,
            resultListener: TransmissionResultListener) =
            channel.transmit(request, resultListener)

    override fun query(
            request: QueryRequest,
            resultListener: TransmissionResultListener) =
            channel.transmit(request, resultListener)

    private val channel by lazy { channelFactory.create(id) }
}
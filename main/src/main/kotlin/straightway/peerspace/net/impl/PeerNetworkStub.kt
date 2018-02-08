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

import straightway.peerspace.Infrastructure
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.net.Peer

/**
 * Implementation of a network stub for a peer.
 *
 * This network stub cares for transmitting data to the physical
 * network node the peer runs on.
 */
class PeerNetworkStub(override val id: Id) : Peer {

    override fun push(data: Chunk) {
        val channel = Infrastructure.instance.channelFactory.create(id)
        channel.transmit(data)
    }
}
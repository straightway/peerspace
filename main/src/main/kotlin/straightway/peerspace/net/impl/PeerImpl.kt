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
import straightway.peerspace.data.Key
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import java.io.Serializable

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl(override val id: Id) : Peer {

    override fun push(request: PushRequest) {
        storedData[request.chunk.key] = request.chunk.data
    }

    fun getData(key: Key): Serializable = storedData[key]!!

    private val storedData = mutableMapOf<Key, Serializable>()
}
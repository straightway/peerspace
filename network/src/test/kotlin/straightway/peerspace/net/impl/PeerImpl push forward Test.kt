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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.PushRequest
import straightway.testing.bdd.Given

class `PeerImpl push forward Test` {

    companion object {
        val peerId = Id("peerId")
        val knownPeersIds = ids("1", "2", "3")
        val chunk = Chunk(Key(Id("chunkId")), byteArrayOf(1, 2, 3))
        val forwardedPeers = 1..2
    }

    private val test get() = Given {
        PeerTestEnvironmentImpl(
                peerId,
                knownPeersIds = knownPeersIds,
                forwardStrategy = mock {
                    on { getPushForwardPeerIdsFor(any()) }
                            .thenReturn(knownPeersIds.slice(forwardedPeers))
                })
    }

    @Test
    fun `push request is forwarded according to forward strategy`() =
            test when_ {
                sut.push(PushRequest(chunk))
            } then {
                verify(forwardStrategy).getPushForwardPeerIdsFor(chunk.key)
            }

    @Test
    fun `push request is forwarded to peers returned by forward strategy`() =
            test when_ {
                sut.push(PushRequest(chunk))
            } then {
                forwardedPeers.forEach {
                    verify(knownPeers[it]).push(PushRequest(chunk))
                }
            }
}

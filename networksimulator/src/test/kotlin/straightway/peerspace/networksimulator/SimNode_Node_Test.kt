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

package straightway.peerspace.networksimulator

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.sim.net.Message
import straightway.sim.net.TransmissionStream
import straightway.testing.bdd.Given
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.units.byte
import straightway.units.get

class SimNode_Node_Test {

    private val test get() = Given {
        object {
            val upload = mock<TransmissionStream>()
            val download = mock<TransmissionStream>()
            val peerId = "id"
            val peer = mock<Peer> { on { id }.thenReturn(peerId) }
            val peers = mutableMapOf(Pair(peerId, peer))
            val chunk = Chunk(Key("chunkId"), "Hello")
            val request = PushRequest(chunk)
            val sut = SimNode(
                    peerId, peers, mock(), { 16[byte] }, upload, download, mutableMapOf())
        }
    }

    @Test
    fun `upload stream is as specified`() =
            test when_ { sut.uploadStream } then { it.result is_ Same as_ upload }

    @Test
    fun `download stream is as specified`() =
            test when_ { sut.downloadStream } then { it.result is_ Same as_ download }

    @Test
    fun `receiving a push requests calls push on parent peer`() =
            test when_ {
                sut.notifyReceive(
                        mock(),
                        Message(request, size = 50[byte]))
            } then {
                verify(peer).push(request)
            }

    @Test
    fun `receiving invalid message panics`() =
            test when_ {
                sut.notifyReceive(mock(), Message("invalid", size = 50[byte]))
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }
}
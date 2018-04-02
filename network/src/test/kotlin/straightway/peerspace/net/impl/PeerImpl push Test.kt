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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class `PeerImpl push Test` {

    private companion object {
        val peerId = Id("peerId")
        val chunkId = Id("chunkId")
        val data = "Data".toByteArray()
    }

    private val test get() = Given {
        object {
            val dataStore = mock<DataChunkStore>()
            val sut = PeerImpl(peerId, dataStore, mock(), mock())
            val chunk = Chunk(Key(chunkId), data)
        }
    }

    @Test
    fun `PeerImpl implements Peer`() =
            test when_ { sut as Peer } then {
                expect ({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `id passed on construction is accessible`() =
            test when_ { sut.id } then { expect(it.result is_ Equal to_ peerId) }

    @Test
    fun `push does not throw`() =
            test when_ { sut.push(PushRequest(chunk)) } then {
                expect ({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `pushed data is stored`() =
            test when_ { sut.push(PushRequest(chunk)) } then {
                verify(dataStore).store(chunk)
            }
}
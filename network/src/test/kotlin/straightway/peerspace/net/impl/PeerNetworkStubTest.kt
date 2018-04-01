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
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Factory
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.untimedData
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class PeerNetworkStubTest {

    private companion object {
        val peerId = Id("id")
    }

    private val test get() = Given {
        object {
            val channel = mock<Channel>()
            val channelFactoryMock = mock<Factory<Channel>> {
                on { create(peerId) }.thenReturn(channel)
            }
            val sut = PeerNetworkStub(peerId, channelFactoryMock)
            val data = Chunk(Key(Id("Key")), arrayOf(1, 2, 3))
            val pushRequest = PushRequest(data)
            val queryRequest = QueryRequest(Id("originatorId"), data.key.id, untimedData)
        }
    }

    @Test
    fun `has specified id`() =
            test when_ { sut.id } then { expect(it.result is_ Equal to_ peerId) }

    @Test
    fun `push creates channel`() =
            test when_ { sut.push(pushRequest) } then {
                verify(channelFactoryMock).create(peerId)
            }

    @Test
    fun `push transmits request on channel`() =
            test when_ { sut.push(pushRequest) } then {
                verify(channel).transmit(pushRequest)
            }

    @Test
    fun `query transmits request on channel`() =
            test when_ { sut.query(queryRequest) } then {
                verify(channel).transmit(queryRequest)
            }
}
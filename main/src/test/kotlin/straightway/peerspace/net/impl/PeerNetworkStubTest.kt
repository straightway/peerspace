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
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.peerspace.Infrastructure
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.Channel
import straightway.peerspace.net.ChannelFactory
import straightway.testing.TestBase
import straightway.testing.flow.equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class PeerNetworkStubTest : TestBase<PeerNetworkStubTest.Environment>() {

    class Environment {
        var channelMockFactoryInvocations = 0
        private val peerId = "id"
        val sut = PeerNetworkStub(peerId)
        private val channelFactoryMock = mock<ChannelFactory> {
            on { create(any()) } doAnswer {
                ++channelMockFactoryInvocations
                channelMock
            }
        }
        val transmittedData = mutableListOf<Any>()
        private val channelMock = mock<Channel> {
            on { transmit(any()) } doAnswer { transmittedData.add(it.arguments[0]); null }
        }
        val data = Chunk(Key("Key"), arrayOf(1, 2, 3))

        init {
            Infrastructure.instance = Infrastructure {
                channelFactory = channelFactoryMock
            }
        }
    }

    @BeforeEach
    fun setup() {
        sut = Environment()
    }

    @Test
    fun hasSpecifiedId() = sut.run {
        expect(sut.id is_ equal to_ "id")
    }

    @Test
    fun push_createsChannel() = sut.run {
        sut.push(data)
        expect(channelMockFactoryInvocations is_ equal to_ 1)
    }

    @Test
    fun push_transmitsRequestOnChannel() = sut.run {
        sut.push(data)
        expect(transmittedData is_ equal to_ listOf(data))
    }
}
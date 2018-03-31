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
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Factory
import straightway.testing.bdd.Given
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class PeerStubFactoryTest {

    private companion object {
        val peerId = Id("id")
    }

    private val test get() = Given {
        object {
            var channelFactory = mock<Factory<Channel>>()
            val sut = PeerStubFactory(channelFactory)
        }
    }

    @Test
    fun `creates PeerNetworkStub instances`() = test when_ {
        sut.create(peerId)
    } then {
        expect(it.result::class is_ Same as_ PeerNetworkStub::class)
    }

    @Test
    fun `created Peer has proper Id`() = test when_ {
        sut.create(peerId)
    } then {
        expect(it.result.id is_ Equal to_ peerId)
    }

    @Test
    fun `uses passes channel factory`() = test when_ {
        sut.create(peerId)
    } then {
        expect(it.result.channelFactory is_ Same as_ channelFactory)
    }
}
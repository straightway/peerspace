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
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.Peer
import straightway.testing.TestBase
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class NetworkImplTest : TestBase<NetworkImplTest.Environment>() {

    class Environment {

        val infrastructure = Infrastructure {
            peerStubFactory = mock {
                on { create(any()) } doAnswer { peerMockFactory(it.arguments[0] as Id) }
            }
        }
        val network = NetworkImpl(infrastructure)
        var peerMockFactory = { id: Id ->
            mock<Peer> { on { id } doReturn id }
        }
    }

    @BeforeEach
    fun setup() {
        sut = Environment()
    }

    @Test
    fun peer_callsPeerFactory() = sut.run {
        var newPeer: Peer? = null
        peerMockFactory = { newId: Id ->
            expect(newPeer is_ Null)
            newPeer = mock { on { id } doReturn newId }
            newPeer!!
        }
        val receiverId = Id("receiver")
        val receiverFromNetwork = network.getPeer(receiverId)
        expect(receiverFromNetwork.id is_ Equal to_ receiverId)
        expect(receiverFromNetwork is_ Same as_ newPeer!!)
    }
}
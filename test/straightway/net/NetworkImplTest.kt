/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.integrationtest

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.data.*
import straightway.infrastructure.*
import straightway.net.*
import straightway.testing.*
import straightway.testing.flow.*

class NetworkImplTest : TestBase<NetworkImplTest.Environment>() {

    class Environment {

        val network = NetworkImpl()
        var peerMockFactory = { id: Id ->
            mock<Peer> { on { id } doReturn id }
        }

        init {
            Infrastructure.instance = Infrastructure {
                peerFactory = mock<PeerFactory> {
                    on { create(any()) } doAnswer { peerMockFactory(it.arguments[0] as Id) }
                }
            }
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
            expect(newPeer _is _null)
            newPeer = mock<Peer> { on { id } doReturn newId }
            newPeer!!
        }
        val receiverFromNetwork = network.peer("receiver")
        expect(receiverFromNetwork.id _is equal _to "receiver")
        expect(receiverFromNetwork _is same _as newPeer!!)
    }
}
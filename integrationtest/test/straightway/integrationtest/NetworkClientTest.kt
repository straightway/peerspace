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

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import straightway.Identifyable
import straightway.data.Chunk
import straightway.data.Key
import straightway.dsl.minus
import straightway.network.PushTarget
import straightway.testing.flow.*

class NetworkClientTest {

    @Test fun id() {
        val sut = NetworkClient("client") as Identifyable
        expect(sut.id _is equal to "client")
    }

    @Test fun push_isAccepted() {
        val sut = NetworkClient("client") as PushTarget

        val peer = mock(Identifyable::class.java)
        `when`(peer.id).thenReturn("node")

        val data = Chunk(Key("0815"), arrayOf(1, 2, 3))

        expect({ sut.push(data, peer) } does not-_throw-exception)
    }
}
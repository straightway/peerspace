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
package straightway.peerspace.integrationtest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.PushRequest
import straightway.testing.TestBase
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class NetworkNodeTest : TestBase<NetworkClient>() {

    private companion object {
        val peerId = Id("peer")
    }
    @BeforeEach
    fun setup() {
        sut = NetworkClient(peerId)
    }

    @Test
    fun id() = expect(sut.id is_ Equal to_ peerId)

    @Test
    fun receiveData_isRegistered() {
        val data = Chunk(Key(Id("0815")), byteArrayOf(1, 2, 3))
        sut.push(PushRequest(data))
        expect(sut.receivedData is_ Equal to_ listOf(data))
    }
}
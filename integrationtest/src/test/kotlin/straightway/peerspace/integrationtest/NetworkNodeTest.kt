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
import straightway.peerspace.data.Key
import straightway.testing.TestBase
import straightway.testing.flow.equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class NetworkNodeTest : TestBase<NetworkClient>() {

    @BeforeEach
    fun setup() {
        sut = NetworkClient("peer")
    }

    @Test
    fun id() = expect(sut.id is_ equal to_ "peer")

    @Test
    fun receiveData_isRegistered() {
        val data = Chunk(Key("0815"), arrayOf(1, 2, 3))
        sut.push(data)
        expect(sut.receivedData is_ equal to_ listOf(data))
    }
}
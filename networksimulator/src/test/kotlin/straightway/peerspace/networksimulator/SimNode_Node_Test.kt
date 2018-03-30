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
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.QuerySource
import straightway.peerspace.net.untimedData
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
import java.io.Serializable

class SimNode_Node_Test {

    private companion object {
        val peerId = Id("id")
        const val chunkData = "Hello"
        val chunkKey = Key(Id("chunkId"))
        val messageSize = 50[byte]
        val invalidRequest = object { override fun toString() = "Invalid Request" }
        val getChunkSize = { _: Serializable -> 16[byte] }
    }

    private val test get() = Given {
        object {
            val upload = mock<TransmissionStream>()
            val download = mock<TransmissionStream>()
            val pushTarget = mock<PushTarget>()
            val pushTargets = mutableMapOf(Pair(peerId, pushTarget))
            val chunk = Chunk(chunkKey, chunkData)
            val pushRequest = PushRequest(chunk)
            val querySource = mock<QuerySource>()
            val querySources = mutableMapOf(Pair(peerId, querySource))
            val queryRequest = QueryRequest(Id("originId"), Id("chunkId"), untimedData)
            val sut = SimNode(
                    peerId,
                    pushTargets,
                    querySources,
                    mock(),
                    getChunkSize,
                    upload,
                    download,
                    mutableMapOf())
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
                sut.notifyReceive(mock(), Message(pushRequest, messageSize))
            } then {
                verify(pushTarget).push(pushRequest)
            }

    @Test
    fun `receiving a query requests calls query on parent peer`() =
            test when_ {
                sut.notifyReceive(mock(), Message(queryRequest, messageSize))
            } then {
                verify(querySource).query(queryRequest)
            }

    @Test
    fun `receiving message with invalid request panics`() =
            test when_ {
                sut.notifyReceive(mock(), Message(invalidRequest, messageSize))
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }
}
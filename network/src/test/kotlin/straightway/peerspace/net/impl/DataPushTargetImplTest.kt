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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect

class DataPushTargetImplTest : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("peerId")
        val chunkId = Id("chunkId")
        val data = "Data".toByteArray()
        val chunk = Chunk(Key(chunkId), data)
    }

    private val test get() = Given {
        object {
            val environment = PeerTestEnvironment(
                dataPushTargetFactory = { DataPushTargetImpl() })
            val sut: DataPushTarget = environment.get()
        }
    }

    @Test
    fun `push does not throw`() =
            test when_ { sut.push(DataPushRequest(peerId, chunk)) } then {
                expect ({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `pushed data is stored`() =
            test when_ { sut.push(DataPushRequest(peerId, chunk)) } then {
                verify(environment.get<DataChunkStore>()).store(chunk)
            }

    @Test
    fun `push notifies resultListener of success`() {
        val resultListener = mock<TransmissionResultListener>()
        test when_ { sut.push(DataPushRequest(peerId, chunk), resultListener) } then {
            verify(resultListener).notifySuccess()
            verify(resultListener, never()).notifyFailure()
        }
    }

    @Test
    fun `push executes pending network requests`() =
            test when_ {
                sut.push(DataPushRequest(peerId, chunk))
            } then {
                verify(environment.get<Network>()).executePendingRequests()
            }

    @Test
    fun `originator of push request is added to known peers`() =
            test when_ {
                sut.push(DataPushRequest(peerId, chunk))
            } then {
                verify(environment.get<PeerDirectory>()).add(peerId)
            }
}
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
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
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
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.testing.bdd.Given
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect

class DataPushTargetImplTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originator")
        val chunkId = Id("chunkId")
        val data = "Data".toByteArray()
        val chunk = Chunk(Key(chunkId), data)
    }

    private val test get() = Given {
        object {
            var epochs = listOf(0)
            var chunkKey = Key(chunkId)
            val pushRequest by lazy {
                DataPushRequest(originatorId, Chunk(chunkKey, byteArrayOf()))
            }
            val environment = PeerTestEnvironment(
                dataPushTargetFactory = { DataPushTargetImpl() }) {
                bean { _ ->
                    mock<EpochAnalyzer> { _ ->
                        on { getEpochs(any()) }.thenAnswer { epochs }
                    }
                }
            }
            val sut: DataPushTarget = environment.get()
            val dataQueryHandler =
                    environment.get<DataQueryHandler>("dataQueryHandler")
            val pushForwardTracker =
                    environment.get<ForwardStateTracker<DataPushRequest>>("pushForwardTracker")
        }
    }

    @Test
    fun `push does not throw`() =
            test when_ { sut.push(DataPushRequest(originatorId, chunk)) } then {
                expect ({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `pushed data is stored`() =
            test when_ { sut.push(DataPushRequest(originatorId, chunk)) } then {
                verify(environment.get<DataChunkStore>()).store(chunk)
            }

    @Test
    fun `push executes pending network requests`() =
            test when_ {
                sut.push(DataPushRequest(originatorId, chunk))
            } then {
                verify(environment.get<Network>()).executePendingRequests()
            }

    @Test
    fun `originator of push request is added to known peers`() =
            test when_ {
                sut.push(DataPushRequest(originatorId, chunk))
            } then {
                verify(environment.get<PeerDirectory>()).add(originatorId)
            }

    @Test
    fun `forwarding is handled by pushForwardTracker`() =
            test when_ {
                sut.push(pushRequest)
            } then {
                verify(pushForwardTracker).forward(pushRequest)
            }

    @Test
    fun `forwarding notifies dataQueryHandler`() =
            test when_ {
                sut.push(pushRequest)
            } then {
                verify(dataQueryHandler).notifyChunkForwarded(pushRequest.chunk.key)
            }

    @Test
    fun `data chunks belonging to multiple epochs are split`() =
            test while_ {
                epochs = listOf(0, 1)
                chunkKey = chunkKey.copy(timestamp = 83L)
            } when_ {
                sut.push(pushRequest)
            } then {
                inOrder(pushForwardTracker) {
                    verify(pushForwardTracker).forward(eq(pushRequest.withEpoch(0)))
                    verify(pushForwardTracker).forward(eq(pushRequest.withEpoch(1)))
                }
            }
}
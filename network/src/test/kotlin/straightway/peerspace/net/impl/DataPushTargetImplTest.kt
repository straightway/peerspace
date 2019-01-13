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
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.Request
import straightway.peerspace.net.chunkSize
import straightway.peerspace.net.dataChunkStore
import straightway.peerspace.net.dataPushTarget
import straightway.peerspace.net.dataQueryHandler
import straightway.peerspace.net.network
import straightway.peerspace.net.peerDirectory
import straightway.peerspace.net.pushForwarder
import straightway.testing.bdd.Given
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.units.byte
import straightway.units.get
import straightway.units.plus

class DataPushTargetImplTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originator")
        val chunkId = Id("chunkId")
        val data = "Data".toByteArray()
        val chunk = DataChunk(Key(chunkId), data)
    }

    private val test get() = Given {
        object {
            var epochs = listOf(0)
            var chunkKey = Key(chunkId)
            val pushRequest by lazy {
                Request(originatorId, DataChunk(chunkKey, byteArrayOf()))
            }
            val environment = PeerTestEnvironment(
                dataPushTargetFactory = { DataPushTargetImpl() }) {
                bean {
                    mock<EpochAnalyzer> {
                        on { getEpochs(any()) }.thenAnswer { epochs }
                    }
                }
            }
            val sut = environment.dataPushTarget
            val dataQueryHandler = environment.dataQueryHandler
            val pushForwarder = environment.pushForwarder
        }
    }

    @Test
    fun `push by default does not throw`() =
            test when_ { sut.pushDataChunk(Request(originatorId, chunk)) } then {
                expect({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `push with too large chunk panics`() =
            test when_ {
                val data = ByteArray((chunkSize + 1[byte]).baseValue.toInt())
                val chunk = DataChunk(Key(originatorId), data)
                sut.pushDataChunk(Request(originatorId, chunk))
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `pushed data is stored`() =
            test when_ { sut.pushDataChunk(Request(originatorId, chunk)) } then {
                verify(environment.dataChunkStore).store(chunk)
            }

    @Test
    fun `push executes pending network requests`() =
            test when_ {
                sut.pushDataChunk(Request(originatorId, chunk))
            } then {
                verify(environment.network).executePendingRequests()
            }

    @Test
    fun `originator of push request is added to known peers`() =
            test when_ {
                sut.pushDataChunk(Request(originatorId, chunk))
            } then {
                verify(environment.peerDirectory).add(originatorId)
            }

    @Test
    fun `forwarding is handled by pushForwardTracker`() =
            test when_ {
                sut.pushDataChunk(pushRequest)
            } then {
                verify(pushForwarder).forward(pushRequest)
            }

    @Test
    fun `forwarding notifies dataQueryHandler`() =
            test when_ {
                sut.pushDataChunk(pushRequest)
            } then {
                verify(dataQueryHandler).notifyChunkForwarded(pushRequest.content.key)
            }

    @Test
    fun `data chunks belonging to multiple epochs are split`() =
            test while_ {
                epochs = listOf(0, 1)
                chunkKey = chunkKey.copy(timestamp = 83L)
            } when_ {
                sut.pushDataChunk(pushRequest)
            } then {
                inOrder(pushForwarder) {
                    verify(pushForwarder).forward(
                            Request(pushRequest.remotePeerId, pushRequest.content.withEpoch(0)))
                    verify(pushForwarder).forward(
                            Request(pushRequest.remotePeerId, pushRequest.content.withEpoch(1)))
                }
            }
}
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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.untimedData
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.peerspace.data.DataQuery
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.peerspace.net.Peer
import straightway.peerspace.net.Transmittable
import straightway.sim.net.Message
import straightway.sim.net.TransmissionStream
import straightway.testing.bdd.Given
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.units.byte
import straightway.units.get

class SimNode_Node_Test : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("id")
        val chunkData = "Hello".toByteArray()
        val chunkKey = Key(Id("chunkId"))
        val messageSize = 50[byte]
    }

    private val test get() = Given {
        object {
            var isUploadOnline = true
            val upload = mock<TransmissionStream> { _ ->
                on { isOnline }.thenAnswer { isUploadOnline }
            }
            var isDownloadOnline = true
            val download = mock<TransmissionStream> { _ ->
                on { isOnline }.thenAnswer { isDownloadOnline }
            }
            val chunk = DataChunk(chunkKey, chunkData)
            val pushRequest = DataPushRequest(Id("senderId"), chunk)
            val queryRequest = DataQueryRequest(
                    Id("originId"), DataQuery(Id("chunkId"), untimedData))
            val peer = mock<Peer>()
            val sut = withContext {
                bean { peer }
                bean("simNodes") { mutableMapOf<Id, SimNode>() }
                bean("uploadStream") { upload }
                bean("downloadStream") { download }
            }.apply {
                extraProperties["peerId"] = peerId.identifier
            }.make {
                SimNode()
            }
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
                verify(peer).push(pushRequest)
            }

    @Test
    fun `receiving a query requests calls query on parent peer`() =
            test when_ {
                sut.notifyReceive(mock(), Message(queryRequest, messageSize))
            } then {
                verify(peer).query(queryRequest)
            }

    @Test
    fun `receiving message with invalid request does nothing`() =
            test when_ {
                sut.notifyReceive(mock(), Message(mock<Transmittable>(), messageSize))
            } then {
                verify(peer, never()).push(any<DataPushRequest>())
                verify(peer, never()).push(any<KnownPeersPushRequest>())
                verify(peer, never()).query(any<DataQueryRequest>())
                verify(peer, never()).query(any<KnownPeersQueryRequest>())
                expect({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `receiving message with not transmittable content does nothing`() =
            test when_ {
                sut.notifyReceive(mock(), Message(Any(), messageSize))
            } then {
                verify(peer, never()).push(any<DataPushRequest>())
                verify(peer, never()).push(any<KnownPeersPushRequest>())
                verify(peer, never()).query(any<DataQueryRequest>())
                verify(peer, never()).query(any<KnownPeersQueryRequest>())
                expect({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `node is online when both channels are online`() =
            test while_ {
                isUploadOnline = true
                isDownloadOnline = true
            } when_ {
                sut.isOnline
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `node is offline if upload stream is offline`() =
            test while_ {
                isUploadOnline = false
            } when_ {
                sut.isOnline
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `node is offline if download stream is offline`() =
            test while_ {
                isDownloadOnline = false
            } when_ {
                sut.isOnline
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `setting node offline sets upload stream offline`() =
            test while_ {
                isUploadOnline = true
            } when_ {
                sut.isOnline = false
            } then {
                verify(upload).isOnline = false
            }

    @Test
    fun `setting node offline sets download stream offline`() =
            test while_ {
                isDownloadOnline = true
            } when_ {
                sut.isOnline = false
            } then {
                verify(download).isOnline = false
            }

    @Test
    fun `setting node online sets upload stream offline`() =
            test while_ {
                isUploadOnline = false
            } when_ {
                sut.isOnline = true
            } then {
                verify(upload).isOnline = true
            }

    @Test
    fun `setting node online sets download stream offline`() =
            test while_ {
                isDownloadOnline = false
            } when_ {
                sut.isOnline = true
            } then {
                verify(download).isOnline = true
            }

    @Test
    fun `notifySuccess does not throw`() =
            test when_ { sut.notifySuccess(mock()) } then {
                expect({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `notifyFailure does not throw`() =
            test when_ { sut.notifyFailure(mock()) } then {
                expect({ it.result } does Not - Throw.exception)
            }
}
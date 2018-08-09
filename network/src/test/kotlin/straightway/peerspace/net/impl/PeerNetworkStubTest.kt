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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.Channel
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.untimedData
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.io.Serializable

class PeerNetworkStubTest : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("id")
    }

    private val test get() = Given {
        object {
            var peerChannels = listOf<Id>()
            val environment = PeerTestEnvironment {
                factory { PeerNetworkStub(it["id"]) }
                factory {
                    peerChannels += it.get<Id>("id")
                    channel
                }
            }
            val channel = mock<Channel> { _ ->
                on { transmit(any(), any()) }.thenAnswer {
                    transmitCallback(
                            it.arguments[0] as Serializable,
                            it.arguments[1] as TransmissionResultListener)
                }
            }
            var transmitCallback: (Serializable, TransmissionResultListener) -> Unit =
                    { _, _ -> }
            val sut =
                    environment.get<PeerNetworkStub> { mapOf("id" to peerId) }
            val data =
                    Chunk(Key(Id("Key")), byteArrayOf(1, 2, 3))
            val dataPushRequest =
                    DataPushRequest(peerId, data)
            val knownPeersPushRequest =
                    KnownPeersPushRequest(Id("OriginatorId"), listOf(Id("Peer1")))
            val dataQueryRequest =
                    DataQueryRequest(Id("originatorId"), data.key.id, untimedData)
            val knownPeersQueryRequest =
                    KnownPeersQueryRequest(Id("originatorId"))
        }
    }

    @Test
    fun `has specified id`() =
            test when_ { sut.id } then { expect(it.result is_ Equal to_ peerId) }

    @Test
    fun `push creates channel`() =
            test when_ { sut.push(dataPushRequest) } then {
                expect(peerChannels is_ Equal to_ Values(peerId))
            }

    @Test
    fun `push transmits data request on channel`() =
            test when_ { sut.push(dataPushRequest) } then {
                verify(channel).transmit(dataPushRequest)
            }

    @Test
    fun `push transmits knownPeers request on channel`() =
            test when_ { sut.push(knownPeersPushRequest) } then {
                verify(channel).transmit(knownPeersPushRequest)
            }

    @Test
    fun `query transmits data request on channel`() =
            test when_ { sut.query(dataQueryRequest) } then {
                verify(channel).transmit(dataQueryRequest)
            }

    @Test
    fun `query transmits knownPeers request on channel`() =
            test when_ { sut.query(knownPeersQueryRequest) } then {
                verify(channel).transmit(knownPeersQueryRequest)
            }

    @Test
    fun `data push forwards notifications to resultListener`() {
        val resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { _, listener -> listener.notifySuccess() }
        } when_ {
            sut.push(dataPushRequest, resultListener)
        } then {
            verify(resultListener).notifySuccess()
        }
    }

    @Test
    fun `data query forwards notifications to resultListener`() {
        val resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { _, listener -> listener.notifySuccess() }
        } when_ {
            sut.query(dataQueryRequest, resultListener)
        } then {
            verify(resultListener).notifySuccess()
        }
    }

    @Test
    fun `knownPeers push forwards notifications to resultListener`() {
        val resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { _, listener -> listener.notifySuccess() }
        } when_ {
            sut.push(knownPeersPushRequest, resultListener)
        } then {
            verify(resultListener).notifySuccess()
        }
    }

    @Test
    fun `knownPeers query forwards notifications to resultListener`() {
        val resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { _, listener -> listener.notifySuccess() }
        } when_ {
            sut.query(knownPeersQueryRequest, resultListener)
        } then {
            verify(resultListener).notifySuccess()
        }
    }
}
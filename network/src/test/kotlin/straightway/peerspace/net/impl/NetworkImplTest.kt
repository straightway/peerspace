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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Network
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.Transmission
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class NetworkImplTest : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("peerId")
        val receiverId = Id("receiver")
        val pushRequest = DataPushRequest(
                peerId,
                Chunk(Key(Id("ChunkKey")), byteArrayOf()))
    }

    private val test get() =
            Given {
                object {
                    var createdIds = listOf<Id>()
                    val channels = mutableMapOf<Id, Channel>()
                    val transmissionResultListeners = mutableListOf<TransmissionResultListener>()
                    val querySource = mock<DataQuerySource>()
                    val environment = PeerTestEnvironment(
                            peerId = peerId,
                            networkFactory = { NetworkImpl() }
                    ) {
                        bean { KoinModuleComponent() }
                        factory { params ->
                            get<KoinModuleComponent>()
                            val id = params.get<Id>("id")
                            createdIds += id
                            channels.getOrPut(id) {
                                mock { _ ->
                                    on { transmit(any(), any()) }.thenAnswer {
                                        val listener: TransmissionResultListener = it.getArgument(1)
                                        transmissionResultListeners.add(listener)
                                    }
                                }
                            }
                        }
                        factory("networkDataQuerySource") {
                            get<KoinModuleComponent>()
                            createdIds += it.get<Id>("id")
                            querySource
                        }
                    }
                    val sut = environment.get<Network>() as NetworkImpl
                }
            }

    @Test
    fun `scheduleTransmission creates new channel via Koin`() =
            test when_ {
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
                sut.executePendingRequests()
            } then {
                expect(createdIds is_ Equal to_ Values(receiverId))
            }

    @Test
    fun `getQuerySource creates new instance via Koin`() =
            test when_ {
                sut.getQuerySource(receiverId)
            } then {
                expect(createdIds is_ Equal to_ Values(receiverId))
            }

    @Test
    fun `scheduleTransmission does not transmit immediately`() =
            test when_ {
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
            } then {
                expect(receiverId !in channels)
            }

    @Test
    fun `scheduleTransmission executes push after call to executePendingRequests`() =
            test when_ {
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
                sut.executePendingRequests()
            } then {
                verify(channels[receiverId]!!).transmit(eq(pushRequest), any())
            }

    @Test
    fun `scheduleTransmission not transmitted again after calling executePendingRequests again`() =
            test when_ {
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
                sut.executePendingRequests()
                sut.executePendingRequests()
            } then {
                verify(channels[receiverId]!!).transmit(eq(pushRequest), any())
            }

    @Test
    fun `transmitting twice the same data to same target is only executed once`() =
            test when_ {
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
                sut.scheduleTransmission(Transmission(receiverId, pushRequest))
                sut.executePendingRequests()
            } then {
                verify(channels[receiverId]!!).transmit(eq(pushRequest), any())
            }

    @Test
    fun `transmitting the same data to two targets is executed for both`() =
            test when_ {
                sut.scheduleTransmission(Transmission(Id("receiver1"), pushRequest))
                sut.scheduleTransmission(Transmission(Id("receiver2"), pushRequest))
                sut.executePendingRequests()
            } then { _ ->
                expect(channels.size is_ Equal to_ 2)
                channels.values.forEach {
                    verify(it).transmit(eq(pushRequest), any())
                }
            }

    @Test
    fun `single transmission result listener is notified of success`() {
        val listener = mock<TransmissionResultListener>()
        test while_ {
            sut.scheduleTransmission(Transmission(receiverId, pushRequest), listener)
            sut.executePendingRequests()
        } when_ {
            transmissionResultListeners.single().notifySuccess()
        } then {
            verify(listener).notifySuccess()
        }
    }

    @Test
    fun `single transmission result listener is notified of failure`() {
        val listener = mock<TransmissionResultListener>()
        test while_ {
            sut.scheduleTransmission(Transmission(receiverId, pushRequest), listener)
            sut.executePendingRequests()
        } when_ {
            transmissionResultListeners.single().notifyFailure()
        } then {
            verify(listener).notifyFailure()
        }
    }

    @Test
    fun `pushing twice the same data to same target notifies all senders of success`() {
        val listener1 = mock<TransmissionResultListener>()
        val listener2 = mock<TransmissionResultListener>()
        test while_ {
            sut.scheduleTransmission(Transmission(receiverId, pushRequest), listener1)
            sut.scheduleTransmission(Transmission(receiverId, pushRequest), listener2)
            sut.executePendingRequests()
        } when_ {
            transmissionResultListeners.single().notifySuccess()
        } then {
            verify(listener1).notifySuccess()
            verify(listener2).notifySuccess()
        }
    }
}
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
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.Network
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.Event

class ForwardStateTrackerImplTest : KoinLoggingDisabler() {

    private data class Item(override val id: String) : Transmittable

    private companion object {
        val originatorId = Id("originator")
        val request83 = Request(originatorId, Item("83"))
        val request2 = Request(originatorId, Item("2"))
    }

    private data class TransmissionRecord(
            val destination: Id,
            val item: Item,
            val listener: TransmissionResultListener)

    private val test get() = Given {
        object {
            val forwardIds = mutableListOf<Id>()
            val transmissions = mutableListOf<TransmissionRecord>()
            val environment = PeerTestEnvironment(
                    networkFactory = {
                        mock { _ ->
                            on { scheduleTransmission(any(), any()) }.thenAnswer {
                                val transmission = it.arguments[0] as Request<*>
                                val listener = it.arguments[1] as TransmissionResultListener
                                transmissions.add(
                                        TransmissionRecord(
                                                transmission.remotePeerId,
                                                transmission.content as Item,
                                                listener
                                ))
                            }
                        }
                    }
            ) {
                bean("testForwarder") { _ ->
                    mock<Forwarder<Transmittable>> { _ ->
                        on { getForwardPeerIdsFor(any(), any()) }.thenAnswer { forwardIds }
                    }
                }
                bean("testTracker") {
                    ForwardStateTrackerImpl(get<Forwarder<Transmittable>>("testForwarder"))
                            as ForwardStateTracker<Transmittable>
                }
            }

            val sut get() = environment.get<ForwardStateTracker<Item>>("testTracker")
            val forwarder get() = environment.get<Forwarder<Item>>("testForwarder")
            val knownPeersReceivedEvent: Event<KnownPeers> =
                    environment.get("knownPeersReceivedEvent")
        }
    }

    @Test
    fun `initial item state of any item is empty`() =
            test when_ {
                sut.getStateFor("la")
            } then {
                expect(it.result is_ Equal to_ ForwardState())
            }

    @Test
    fun `forward sets item state to pending`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds.toSet()))
            }

    @Test
    fun `forward asks for peers to forward`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                verify(forwarder).getForwardPeerIdsFor(request83, ForwardState())
            }

    @Test
    fun `forward passes old forwardState when asking for peers to forward`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(request83)
                clearInvocations(forwarder)
            } when_ {
                sut.forward(request83)
            } then {
                verify(forwarder).getForwardPeerIdsFor(request83, ForwardState(
                        pending = setOf(Id("forward"))))
            }

    @Test
    fun `another forward sets item state also to pending`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
                sut.forward(request2)
            } then {
                expect(sut.getStateFor("2") is_ Equal to_
                               ForwardState(pending = forwardIds.toSet()))
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds.toSet()))
            }

    @Test
    fun `forward to multiple targets sets item states to pending`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
            } when_ {
                sut.forward(request83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds.toSet()))
            }

    @Test
    fun `forward the same item with same destinations twice is same as doing it once`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
                sut.forward(request83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds.toSet()))
            }

    @Test
    fun `forward the same item but other destinations again sets new destinations as pending`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                sut.forward(request83)
            } when_ {
                forwardIds.clear()
                forwardIds.add(Id("forward2"))
                sut.forward(request83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        pending = setOf(Id("forward1"), Id("forward2"))))
            }

    @Test
    fun `forward passes item to network`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                verify(environment.get<Network>()).scheduleTransmission(
                        Request(Id("forward"), request83.content),
                        transmissions.single().listener)
            }

    @Test
    fun `successful transmission changes state and keeps state of other transmissions`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
                sut.forward(request83)
            } when_ {
                transmissions.first().listener.notifySuccess()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        successful = forwardIds.slice(0..0).toSet(),
                        pending = forwardIds.slice(1..1).toSet()))
            }

    @Test
    fun `if last transmission was successful, the state is deleted`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(request83)
            } when_ {
                transmissions.first().listener.notifySuccess()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState())
            }

    @Test
    fun `failed transmission changes state and keeps state of other transmissions`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
                sut.forward(request83)
                forwardIds.clear()
            } when_ {
                transmissions.first().listener.notifyFailure()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        failed = setOf(Id("forward1")),
                        pending = setOf(Id("forward2"))))
            }

    @Test
    fun `if last transmission failed, the state is deleted`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(request83)
                forwardIds.clear()
            } when_ {
                transmissions.first().listener.notifyFailure()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState())
            }

    @Test
    fun `item is re-forwarded on failure`() =
        test while_ {
            forwardIds.add(Id("forward1"))
            sut.forward(request83)
        } when_ {
            forwardIds.clear()
            forwardIds.add(Id("forward2"))
            transmissions.first().listener.notifyFailure()
        } then {
            val network = environment.get<Network>()
            val transmission = Request(Id("forward1"), request83.content)
            inOrder(network) {
                verify(network).scheduleTransmission(
                        transmission,
                        transmissions[0].listener)
                verify(network).scheduleTransmission(
                        Request(Id("forward2"), transmission.content),
                        transmissions[1].listener)
                verify(network).executePendingRequests()
            }

            expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                    failed = setOf(Id("forward1")),
                    pending = setOf(Id("forward2"))
            ))
        }

    @Test
    fun `if strategy yields no forward peers, refresh known peers`() =
            test while_ {
                forwardIds.clear()
            } when_ {
                sut.forward(request83)
            } then {
                verify(environment.get<KnownPeersGetter>()).refreshKnownPeers()
            }

    @Test
    fun `if strategy yields no forward peers, retry forwarding after known peers are refreshed`() =
            test while_ {
                forwardIds.clear()
                sut.forward(request83)
            } when_ {
                forwardIds.add(Id("forward"))
                knownPeersReceivedEvent(KnownPeers(listOf()))
            } then {
                verify(environment.get<Network>()).scheduleTransmission(
                        eq(Request(Id("forward"), request83.content)),
                        any())
                verify(environment.get<Network>()).executePendingRequests()
            }

    @Test
    fun `only single forwarding retry after known peers are refreshed`() =
            test while_ {
                forwardIds.clear()
                sut.forward(request83)
            } when_ {
                forwardIds.add(Id("forward"))
                knownPeersReceivedEvent(KnownPeers(listOf()))
                knownPeersReceivedEvent(KnownPeers(listOf()))
            } then {
                verify(environment.get<Network>()).scheduleTransmission(
                        eq(Request(Id("forward"), request83.content)),
                        any())
                verify(environment.get<Network>()).executePendingRequests()
            }
}
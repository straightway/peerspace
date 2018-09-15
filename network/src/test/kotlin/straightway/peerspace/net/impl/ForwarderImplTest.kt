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
import straightway.koinutils.Bean.get
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.ForwardTargetGetter
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.knownPeersGetter
import straightway.peerspace.net.knownPeersReceivedEvent
import straightway.peerspace.net.network
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ForwarderImplTest : KoinLoggingDisabler() {

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

    private val test get() = test(forwardRetries = 1)

    private fun test(forwardRetries: Int) = Given {
        object {
            val forwardIds = mutableListOf<Id>()
            val transmissions = mutableListOf<TransmissionRecord>()
            val forwardStates = mutableMapOf<Any, ForwardState>()
            val environment = PeerTestEnvironment(
                    configurationFactory = {
                        Configuration(forwardRetries = forwardRetries)
                    },
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
                bean("testForwardTargetGetter") { _ ->
                    mock<ForwardTargetGetter> { _ ->
                        on { getForwardPeerIdsFor(any(), any()) }.thenAnswer { forwardIds }
                    }
                }
                bean("testTracker") { _ ->
                    mock<ForwardStateTracker> { _ ->
                        on { get(any()) }.thenAnswer {
                            forwardStates.getOrDefault(it.getArgument(0), ForwardState())
                        }
                        on { set(any(), any()) }.thenAnswer {
                            forwardStates.set(it.getArgument(0), it.getArgument(1))
                        }
                    }
                }
                bean("testForwarder") {
                    ForwarderImpl(get("testTracker"), get("testForwardTargetGetter"))
                }
            }

            val sut get() =
                environment.get<ForwarderImpl>("testForwarder")
            val tracker get() =
                environment.get<ForwardStateTracker>("testTracker")
            val forwardTargetGetter get() =
                environment.get<ForwardTargetGetter>("testForwardTargetGetter")
        }
    }

    @Test
    fun `forward sets item state to pending`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                verify(tracker).set(
                        request83.content.id,
                        ForwardState(pending = setOf(Id("forward"))))
            }

    @Test
    fun `forward asks for peers to forward`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                verify(forwardTargetGetter).getForwardPeerIdsFor(request83, ForwardState())
            }

    @Test
    fun `forward passes old forwardState when asking for peers to forward`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(request83)
                clearInvocations(forwardTargetGetter)
            } when_ {
                sut.forward(request83)
            } then {
                verify(forwardTargetGetter).getForwardPeerIdsFor(request83, ForwardState(
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
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.toSet())
                    verify(tracker)["2"] = ForwardState(pending = forwardIds.toSet())
                }
            }

    @Test
    fun `forward to multiple targets sets item states to pending`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
            } when_ {
                sut.forward(request83)
            } then { _ ->
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.slice(0..0).toSet())
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.toSet())
                }
            }

    @Test
    fun `forward the same item with same destinations twice is same as doing it once`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
                sut.forward(request83)
            } then {
                expect(forwardStates["83"] is_ Equal to_
                               ForwardState(pending = setOf(Id("forward"))))
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
                verify(tracker)["83"] = ForwardState(
                        pending = setOf(Id("forward1"), Id("forward2")))
            }

    @Test
    fun `forward passes item to network`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(request83)
            } then {
                verify(environment.network).scheduleTransmission(
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
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.slice(0..0).toSet())
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.toSet())
                    verify(tracker)["83"] = ForwardState(
                            successful = forwardIds.slice(0..0).toSet(),
                            pending = forwardIds.slice(1..1).toSet())
                }
            }

    @Test
    fun `if last transmission was successful, the state is deleted`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(request83)
            } when_ {
                transmissions.first().listener.notifySuccess()
            } then {
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(pending = forwardIds.toSet())
                    verify(tracker)["83"] = ForwardState(successful = forwardIds.toSet())
                    verify(tracker).clearFinishedTransmissionFor("83")
                }
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
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(
                            pending = setOf(Id("forward1"), Id("forward2")))
                    verify(tracker)["83"] = ForwardState(
                            failed = setOf(Id("forward1")),
                            pending = setOf(Id("forward2")))
                }
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
                inOrder(tracker) {
                    verify(tracker)["83"] = ForwardState(
                            pending = setOf(Id("forward")))
                    verify(tracker)["83"] = ForwardState(
                            failed = setOf(Id("forward")))
                    verify(tracker).clearFinishedTransmissionFor("83")
                }
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
            val transmission = Request(Id("forward1"), request83.content)
            inOrder(environment.network) {
                verify(environment.network).scheduleTransmission(
                        transmission,
                        transmissions[0].listener)
                verify(environment.network).scheduleTransmission(
                        Request(Id("forward2"), transmission.content),
                        transmissions[1].listener)
                verify(environment.network).executePendingRequests()
            }

            expect(tracker["83"] is_ Equal to_ ForwardState(
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
                verify(environment.knownPeersGetter).refreshKnownPeers()
            }

    @Test
    fun `if strategy yields no forward peers, retry forwarding after known peers are refreshed`() =
            test while_ {
                forwardIds.clear()
                sut.forward(request83)
            } when_ {
                forwardIds.add(Id("forward"))
                environment.knownPeersReceivedEvent(KnownPeers(listOf()))
            } then {
                verify(environment.network).scheduleTransmission(
                        eq(Request(Id("forward"), request83.content)),
                        any())
                verify(environment.network).executePendingRequests()
            }

    @Test
    fun `only configured number of forwarding retries after known peers are refreshed`() =
            test(forwardRetries = 1) while_ {
                forwardIds.clear()
                sut.forward(request83)
            } when_ {
                forwardIds.add(Id("forward"))
                environment.knownPeersReceivedEvent(KnownPeers(listOf()))
                environment.knownPeersReceivedEvent(KnownPeers(listOf()))
            } then {
                verify(environment.network).scheduleTransmission(
                        eq(Request(Id("forward"), request83.content)),
                        any())
                verify(environment.network).executePendingRequests()
            }

    @Test
    fun `multiple forwarding retries if necessary and configured`() =
            test(forwardRetries = 2) while_ {
                forwardIds.clear()
                sut.forward(request83)
            } when_ {
                environment.knownPeersReceivedEvent(KnownPeers(listOf()))
                forwardIds.add(Id("forward"))
                environment.knownPeersReceivedEvent(KnownPeers(listOf()))
            } then {
                verify(environment.network).scheduleTransmission(
                        eq(Request(Id("forward"), request83.content)),
                        any())
                verify(environment.network).executePendingRequests()
            }
}
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
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ForwardStateTrackerTest {

    private data class Transmission(
            val destination: Id,
            val item: Int,
            val listener: TransmissionResultListener)

    private val test get() = Given {
        object {
            val forwardIds = mutableListOf<Id>()
            val transmissions = mutableListOf<Transmission>()
            val forwarder = mock<Forwarder<Int, String>> {
                on { getKeyFor(any()) }.thenAnswer { it.arguments[0].toString() }
                on { getForwardPeerIdsFor(any(), any()) }.thenAnswer { forwardIds }
                on { forwardTo(any(), any(), any()) }.thenAnswer {
                    val destinationId = it.arguments[0] as Id
                    val item = it.arguments[1] as Int
                    val listener = it.arguments[2] as TransmissionResultListener
                    val transmission = Transmission(destinationId, item, listener)
                    transmissions.add(transmission)
                }
            }
            val sut = ForwardStateTracker(forwarder)
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
    fun `forward asks forwarder for item key`() =
            test when_ {
                sut.forward(83)
            } then {
                verify(forwarder).getKeyFor(83)
            }

    @Test
    fun `forward sets item state to pending`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds))
            }

    @Test
    fun `forward asks for peers to forward`() =
            test when_ {
                sut.forward(83)
            } then {
                verify(forwarder).getForwardPeerIdsFor(83, ForwardState())
            }

    @Test
    fun `forward passes old forwardState when asking for peers to forward`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(83)
                clearInvocations(forwarder)
            } when_ {
                sut.forward(83)
            } then {
                verify(forwarder).getForwardPeerIdsFor(83, ForwardState(
                        pending = listOf(Id("forward"))))
            }

    @Test
    fun `another forward sets item state also to pending`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(83)
                sut.forward(2)
            } then {
                expect(sut.getStateFor("2") is_ Equal to_
                               ForwardState(pending = forwardIds))
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds))
            }

    @Test
    fun `forward to multiple targets sets item states to pending`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
            } when_ {
                sut.forward(83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds))
            }

    @Test
    fun `forward the same item with same destinations twice is same as doing it once`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(83)
                sut.forward(83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_
                               ForwardState(pending = forwardIds))
            }

    @Test
    fun `forward the same item but other destinations again sets new destinations as pending`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                sut.forward(83)
            } when_ {
                forwardIds.clear()
                forwardIds.add(Id("forward2"))
                sut.forward(83)
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        pending = listOf(Id("forward1"), Id("forward2"))))
            }

    @Test
    fun `forward passes item to forwarder`() =
            test while_ {
                forwardIds.add(Id("forward"))
            } when_ {
                sut.forward(83)
            } then {
                verify(forwarder).forwardTo(
                        Id("forward"), 83, transmissions.single().listener)
            }

    @Test
    fun `successful transmission changes state and keeps state of other transmissions`() =
            test while_ {
                forwardIds.add(Id("forward1"))
                forwardIds.add(Id("forward2"))
                sut.forward(83)
            } when_ {
                transmissions.first().listener.notifySuccess()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        successful = forwardIds.slice(0..0),
                        pending = forwardIds.slice(1..1)))
            }

    @Test
    fun `if last transmission was successful, the state is deleted`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(83)
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
                sut.forward(83)
                forwardIds.clear()
            } when_ {
                transmissions.first().listener.notifyFailure()
            } then {
                expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                        failed = listOf(Id("forward1")),
                        pending = listOf(Id("forward2"))))
            }

    @Test
    fun `if last transmission failed, the state is deleted`() =
            test while_ {
                forwardIds.add(Id("forward"))
                sut.forward(83)
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
            sut.forward(83)
        } when_ {
            forwardIds.clear()
            forwardIds.add(Id("forward2"))
            transmissions.first().listener.notifyFailure()
        } then {
            inOrder(forwarder) {
                verify(forwarder).forwardTo(Id("forward1"), 83, transmissions[0].listener)
                verify(forwarder).forwardTo(Id("forward2"), 83, transmissions[1].listener)
            }

            expect(sut.getStateFor("83") is_ Equal to_ ForwardState(
                    failed = listOf(Id("forward1")),
                    pending = listOf(Id("forward2"))
            ))
        }
}
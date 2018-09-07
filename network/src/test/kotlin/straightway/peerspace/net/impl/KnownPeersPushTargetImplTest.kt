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

import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.Event

class KnownPeersPushTargetImplTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originator")
        val knownPeerIds = listOf(Id("Peer1"), Id("Peer2"))
        val pushRequest = Request(originatorId, KnownPeers(knownPeerIds))
    }

    private val test get() = Given {
        object {
            val environment = PeerTestEnvironment(knownPeersPushTargetFactory = {
                KnownPeersPushTargetImpl()
            })
            val sut: KnownPeersPushTarget = environment.get()
            val peerDirectory: PeerDirectory = environment.get()
            val knownPeersReceivedEvent: Event<KnownPeers> =
                    environment.get("knownPeersReceivedEvent")
        }
    }

    @Test
    fun `push adds originator to peer directory`() =
            test when_ {
                sut.pushKnownPeers(pushRequest)
            } then {
                verify(peerDirectory).add(originatorId)
            }

    @Test
    fun `push adds known peer ids`() =
            test when_ {
                sut.pushKnownPeers(pushRequest)
            } then {
                knownPeerIds.forEach { peerId -> verify(peerDirectory).add(peerId) }
            }

    @Test
    fun `received known peers are published via knownPeersReceivedEvent`() {
        var receivedRequest: KnownPeers? = null
        test while_ {
            knownPeersReceivedEvent.attach {
                expect(receivedRequest is_ Null)
                receivedRequest = it
            }
        } when_ {
            sut.pushKnownPeers(pushRequest)
        } then {
            expect(receivedRequest is_ Equal to_ pushRequest.content)
        }
    }

    @Test
    fun `knownPeersReceivedEvent is fired after known peers were added to peer directory`() {
        test while_ {
            knownPeersReceivedEvent.attach { event ->
                event.knownPeersIds.forEach { verify(peerDirectory).add(it) }
            }
        } when_ {
            sut.pushKnownPeers(pushRequest)
        } then {
            expect({ it.result } does Not - Throw.exception)
        }
    }
}
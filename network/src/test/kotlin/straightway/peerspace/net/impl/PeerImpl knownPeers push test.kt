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
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given

class `PeerImpl knownPeers push test` : KoinLoggingDisabler() {

    private val test get() =
        Given {
            object {
                val environment = PeerTestEnvironment(
                        peerFactory = { PeerImpl() },
                        knownPeersPushTargetFactory = { KnownPeersPushTargetImpl() })
                val sut get() = environment.get<Peer>()
                val peerDirectory = environment.get<PeerDirectory>()
            }
        }

    @Test
    fun `passed known peers are added to known peers`() =
            test when_ {
                sut.push(KnownPeersPushRequest(
                        Id("OriginatorId"),
                        listOf(Id("Peer1"), Id("Peer2"))))
            } then {
                verify(peerDirectory).add(Id("Peer1"))
                verify(peerDirectory).add(Id("Peer2"))
            }

    @Test
    fun `passed originator id is added to known peers`() =
            test when_ {
                sut.push(KnownPeersPushRequest(
                        Id("OriginatorId"),
                        listOf(Id("Peer1"), Id("Peer2"))))
            } then {
                verify(peerDirectory).add(Id("OriginatorId"))
            }

    @Test
    fun `signals success`() {
        val listener = mock<TransmissionResultListener>()
        test when_ {
            sut.push(
                    KnownPeersPushRequest(
                            Id("OriginatorId"),
                            listOf(Id("Peer1"), Id("Peer2"))),
                    listener)
        } then {
            verify(listener).notifySuccess()
        }
    }
}
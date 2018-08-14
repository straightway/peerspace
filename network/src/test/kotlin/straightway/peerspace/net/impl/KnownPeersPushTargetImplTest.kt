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
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.PeerDirectory
import straightway.testing.bdd.Given

class KnownPeersPushTargetImplTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originator")
        val knownPeerIds = listOf(Id("Peer1"), Id("Peer2"))
        val pushRequest = KnownPeersPushRequest(originatorId, knownPeerIds)
    }

    private val test get() = Given {
        object {
            val environment = PeerTestEnvironment(knownPeersPushTargetFactory = {
                KnownPeersPushTargetImpl()
            })
            val sut: KnownPeersPushTarget = environment.get()
            val peerDirectory: PeerDirectory = environment.get()
        }
    }

    @Test
    fun `push adds originator to peer directory`() =
            test when_ {
                sut.push(pushRequest)
            } then {
                verify(peerDirectory).add(originatorId)
            }

    @Test
    fun `push adds known peer ids`() =
            test when_ {
                sut.push(pushRequest)
            } then {
                knownPeerIds.forEach { peerId -> verify(peerDirectory).add(peerId) }
            }
}
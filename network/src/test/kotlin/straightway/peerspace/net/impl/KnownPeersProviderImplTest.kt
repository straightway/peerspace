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
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.KnownPeersQuery
import straightway.peerspace.net.Request
import straightway.peerspace.net.configuration
import straightway.peerspace.net.knownPeersQuerySource
import straightway.testing.bdd.Given

class KnownPeersProviderImplTest : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("PeerId")
        val knownPeerId = Id("knownPeerId")
        val queryingPeerId = Id("QueryingPeerId")
        val query = Request(queryingPeerId, KnownPeersQuery())
    }

    private val test get() = Given {
        PeerTestEnvironment(
                peerId,
                knownPeersIds = listOf(knownPeerId, queryingPeerId),
                knownPeersQuerySourceFactory = { KnownPeersQuerySourceImpl() }
        )
    }

    @Test
    fun `a query for known peers is answered immediately`() =
            test when_ {
                knownPeersQuerySource.queryKnownPeers(query)
            } then {
                verify(queryingPeer).pushKnownPeers(any())
            }

    @Test
    fun `a query for known peers is answered with the list of known peers`() =
            test when_ {
                knownPeersQuerySource.queryKnownPeers(query)
            } then {
                val expectedKnownPeerIds = knownPeersIds
                verify(queryingPeer).pushKnownPeers(
                        argThat {
                            content.knownPeersIds == expectedKnownPeerIds
                        })
            }

    @Test
    fun `a query for known peers yields not more peers than specified`() =
            test andGiven {
                it.copy(
                        configurationFactory = {
                            it.configuration.copy(maxKnownPeersAnswers = 1)
                        },
                        knownPeerAnswerChooserFactory = {
                            createChooser { knownPeersIds.slice(0..0) }
                        })
            } when_ {
                knownPeersQuerySource.queryKnownPeers(query)
            } then {
                val expectedPeerIds = knownPeersIds.slice(0..0)
                verify(queryingPeer).pushKnownPeers(
                        argThat {
                            content.knownPeersIds == expectedPeerIds })
            }

    private val PeerTestEnvironment.queryingPeer get() = getPeer(queryingPeerId)
}
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
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.testing.bdd.Given

class KnownPeersProviderImplTest : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("PeerId")
        val knownPeerId = Id("knownPeerId")
        val queryingPeerId = Id("QueryingPeerId")
    }

    private val test get() = Given {
        PeerTestEnvironment(
                peerId,
                knownPeersIds = listOf(knownPeerId, queryingPeerId),
                knownPeersProviderFactory = { KnownPeersProviderImpl() }
        )
    }

    @Test
    fun `a query for known peers is answered immediately`() =
            test when_ {
                get<KnownPeersProvider>().pushKnownPeersTo(queryingPeerId)
            } then {
                verify(queryingPeer).push(any<KnownPeersPushRequest>(), any())
            }

    @Test
    fun `a query for known peers is answered with the list of known peers`() =
            test when_ {
                get<KnownPeersProvider>().pushKnownPeersTo(queryingPeerId)
            } then {
                val expectedKnownPeerIds = knownPeersIds
                verify(queryingPeer).push(
                        argThat<KnownPeersPushRequest> {
                            knownPeersIds == expectedKnownPeerIds
                        },
                        any())
            }

    @Test
    fun `a query for known peers yields not more peers than specified`() =
            test andGiven {
                it.copy(
                        configurationFactory = {
                            it.get<Configuration>().copy(maxKnownPeersAnswers = 1)
                        },
                        knownPeerAnswerChooserFactory = {
                            createChooser { knownPeersIds.slice(0..0) }
                        })
            } when_ {
                get<KnownPeersProvider>().pushKnownPeersTo(queryingPeerId)
            } then {
                val expectedPeerIds = knownPeersIds.slice(0..0)
                verify(queryingPeer).push(
                        argThat<KnownPeersPushRequest> {
                            knownPeersIds == expectedPeerIds },
                        any())
            }

    private val PeerTestEnvironment.queryingPeer get() = getPeer(queryingPeerId)
}
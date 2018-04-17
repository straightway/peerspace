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

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given

class `PeerImpl refreshKnownPeers Test` {

    private companion object {
        val peerId = Id("PeerId")
        val knownPeerId = Id("knownPeerId")
        val knownPeersRequest = QueryRequest(peerId, Administrative.KnownPeers)
    }

    private val defaultEnvironment = PeerTestEnvironmentImpl(
            peerId,
            knownPeersIds = listOf(knownPeerId),
            configuration = Configuration(maxPeersToQueryForKnownPeers = 2))

    private val test get() = Given { defaultEnvironment }

    @Test
    fun `refreshKnownPeers queries peer from peerDirectory`() =
            test when_ { peer.refreshKnownPeers() } then {
                verify(knownPeers.single()).query(knownPeersRequest)
            }

    @Test
    fun `a second call to refreshKnownPeers is effective`() =
            test while_ {
                peer.refreshKnownPeers()
            } when_ {
                peer.refreshKnownPeers()
            } then {
                verify(knownPeers.single(), times(2)).query(knownPeersRequest)
            }

    @Test
    fun `number of peers queried for knownPeers is determined by configuration`() =
            Given {
                defaultEnvironment.copy(
                        knownPeersIds = ids(knownPeerId.identifier, "1", "2"))
            } when_ {
                peer.refreshKnownPeers()
            } then {
                knownPeers.take(configuration.maxPeersToQueryForKnownPeers).forEach {
                    verify(it).query(knownPeersRequest)
                }
                knownPeers.drop(configuration.maxPeersToQueryForKnownPeers).forEach {
                    verify(it, never()).query(knownPeersRequest)
                }
            }

    @Test
    fun `set peers to query for other known peers is randomized`() =
            Given {
                defaultEnvironment.copy(
                        knownPeersIds = ids(knownPeerId.identifier, "1", "2")
                ).apply {
                    knownPeerQueryChooser = createChooser {
                        listOf(knownPeers[0].id, knownPeers[2].id)
                    }
                }
            } when_ {
                peer.refreshKnownPeers()
            } then {
                verify(knownPeers[0]).query(knownPeersRequest)
                verify(knownPeers[1], never()).query(knownPeersRequest)
                verify(knownPeers[2]).query(knownPeersRequest)
            }
}
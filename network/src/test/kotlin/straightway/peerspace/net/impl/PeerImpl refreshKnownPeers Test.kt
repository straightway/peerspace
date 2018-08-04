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
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.testing.bdd.Given

class `PeerImpl refreshKnownPeers Test` : KoinLoggingDisabler() {

    private companion object {
        val peerId = Id("PeerId")
        val knownPeerId = Id("knownPeerId")
        val knownPeersRequest = DataQueryRequest(peerId, Administrative.KnownPeers)
    }

    private val test get() = Given {
        PeerTestEnvironment(
            peerId,
            peerFactory = { PeerImpl() },
            knownPeersIds = listOf(knownPeerId),
            configurationFactory = { Configuration(maxPeersToQueryForKnownPeers = 2) })
    }

    private val PeerTestEnvironment.peerImpl get() = get<Peer>() as PeerImpl
    private val PeerTestEnvironment.peerDirectory get() = get<PeerDirectory>()

    @Test
    fun `refreshKnownPeers queries peer from peerDirectory`() =
            test when_ { peerImpl.refreshKnownPeers() } then {
                verify(knownPeers.single()).query(knownPeersRequest)
            }

    @Test
    fun `a second call to refreshKnownPeers is effective`() =
            test while_ {
                peerImpl.refreshKnownPeers()
            } when_ {
                peerImpl.refreshKnownPeers()
            } then {
                verify(knownPeers.single(), times(2)).query(knownPeersRequest)
            }

    @Test
    fun `number of peers queried for knownPeers is determined by configuration`() =
            Given {
                PeerTestEnvironment(
                        peerId,
                        peerFactory = { PeerImpl() },
                        knownPeersIds = ids(knownPeerId.identifier, "1", "2"),
                        configurationFactory = {
                            Configuration(maxPeersToQueryForKnownPeers = 2)
                        })
            } when_ {
                peerImpl.refreshKnownPeers()
            } then {
                knownPeers.take(get<Configuration>().maxPeersToQueryForKnownPeers).forEach {
                    verify(it).query(knownPeersRequest)
                }
                knownPeers.drop(get<Configuration>().maxPeersToQueryForKnownPeers).forEach {
                    verify(it, never()).query(knownPeersRequest)
                }
            }

    @Test
    fun `set peers to query for other known peers is randomized`() =
            Given {
                PeerTestEnvironment(
                        peerId,
                        peerFactory = { PeerImpl() },
                        knownPeersIds = ids(knownPeerId.identifier, "1", "2"),
                        configurationFactory = {
                            Configuration(maxPeersToQueryForKnownPeers = 2)
                        },
                        knownPeerQueryChooserFactory = {
                            createChooser {
                                listOf(knownPeers[0].id, knownPeers[2].id)
                            }
                        })
            } when_ {
                peerImpl.refreshKnownPeers()
            } then {
                verify(knownPeers[0]).query(knownPeersRequest)
                verify(knownPeers[1], never()).query(knownPeersRequest)
                verify(knownPeers[2]).query(knownPeersRequest)
            }

    @Test
    fun `originator of push request is added to known peers`() =
            test when_ {
                peerImpl.push(
                        DataPushRequest(
                                knownPeerId,
                                Chunk(Key(Id("chunkId")), byteArrayOf())))
            } then {
                verify(peerDirectory).add(knownPeerId)
            }

    @Test
    fun `originator of query request is added to known peers`() =
            test when_ {
                peerImpl.query(DataQueryRequest(knownPeerId, Id("chunkId")))
            } then {
                verify(peerDirectory).add(knownPeerId)
            }
}
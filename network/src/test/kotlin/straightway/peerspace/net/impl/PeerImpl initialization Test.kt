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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given

class `PeerImpl initialization Test` {

    private companion object {
        val peerId = Id("PeerId")
        val knownPeerId = Id("knownPeerId")
        val knownPeersRequest = QueryRequest(peerId, Administrative.KnownPeers)
    }

    private val test get() = Given {
        object {
            val peers = mutableListOf(mock<Peer> { on { id }.thenReturn(knownPeerId) })
            val network = mock<Network> {
                on { getQuerySource(any()) }.thenAnswer { args ->
                    peers.find { it.id == args.arguments[0] }
                }
                on { getPushTarget(any()) }.thenAnswer { args ->
                    peers.find { it.id == args.arguments[0] }
                }
            }
            val peerDirectory = mock<PeerDirectory> {
                on { allKnownPeersIds }.thenAnswer {
                    peers.map { it.id }
                }
            }
            val chunk = Chunk(Key(knownPeerId), byteArrayOf(1, 2, 3))
            val dataChunkStore = mock<DataChunkStore> {
                onGeneric { query(any()) }.thenReturn(listOf(chunk))
            }
            var configuration = Configuration(maxPeersToQueryForKnownPeers = 2)
            val sut = PeerImpl(
                    peerId,
                    dataChunkStore = dataChunkStore,
                    peerDirectory = peerDirectory,
                    network = network,
                    configuration = configuration)
        }
    }

    @Test
    fun `refreshKnownPeers queries peer from peerDirectory`() =
            test when_ { sut.refreshKnownPeers() } then {
                verify(peers.single()).query(knownPeersRequest)
            }

    @Test
    fun `a second call to refreshKnownPeers is effective`() =
            test while_ {
                sut.refreshKnownPeers()
            } when_ {
                sut.refreshKnownPeers()
            } then {
                verify(peers.single(), times(2)).query(knownPeersRequest)
            }

    @Test
    fun `number of peers queried for knownPeers is determined by configuration`() =
            test while_ {
                peers += mock<Peer> { on { id }.thenReturn(Id("1")) }
                peers += mock<Peer> { on { id }.thenReturn(Id("2")) }
            } when_ {
                sut.refreshKnownPeers()
            } then {
                peers.take(configuration.maxPeersToQueryForKnownPeers).forEach {
                    verify(it).query(knownPeersRequest)
                }
                peers.drop(configuration.maxPeersToQueryForKnownPeers).forEach {
                    verify(it, never()).query(knownPeersRequest)
                }
            }

    @Test
    fun `set peers to query for other known peers is randomized`() =
            test while_ {
                peers += mock<Peer> { on { id }.thenReturn(Id("1")) }
                peers += mock<Peer> { on { id }.thenReturn(Id("2")) }
            } when_ {
                sut.refreshKnownPeers()
            } then {
                verify(peers[0]).query(knownPeersRequest)
                verify(peers[1], never()).query(knownPeersRequest)
                verify(peers[2]).query(knownPeersRequest)
            }
}
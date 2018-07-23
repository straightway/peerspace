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
import straightway.koinutils.withContext
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.PeerDirectory
import straightway.random.Chooser

class ForwardStrategyTestEnvironment {
    companion object {
        val peerId = Id("peerId")
        val chunkId = Id("chunkId")
        val chunkKey = Key(chunkId)
        val otherPeerIds = mapOf(
                Id("otherId+250") to 250L,
                Id("otherId+200") to 200L,
                Id("otherId+150") to 150L,
                Id("otherId+100") to 100L,
                Id("otherId+50") to 50L,
                Id("otherId-100") to -100L)
        val idForHash = otherPeerIds.map { mapEntry -> mapEntry.value to mapEntry.key }.toMap()
        const val chunkKeyHash = 100L
    }

    val hashes = mutableMapOf<KeyHashable, List<Long>>(
            Key(peerId) to listOf(0L),
            chunkKey to listOf(chunkKeyHash))
    val knownPeerIds = mutableSetOf<Id>()
    var chosenForwardPeers: List<Id>? = null
    val forwardPeerChooser = mock<Chooser> {
        on { chooseFrom<Id>(any(), any()) }.thenAnswer {
            chosenForwardPeers ?: it.arguments[0]
        }
    }
    val configuration = Configuration(numberOfForwardPeers = 2)
    var forwardState = ForwardState()
    val sut = withContext {
        bean {
            mock<PeerDirectory> {
                on { allKnownPeersIds }.thenAnswer { knownPeerIds }
            }
        }
        bean {
            mock<KeyHasher> {
                on { getHashes(any()) }.thenAnswer {
                    hashes[it.arguments[0]]!!
                }
            }
        }
        bean { configuration }
        bean("forwardPeerChooser") { forwardPeerChooser }
    }.apply {
        extraProperties["peerId"] = peerId.identifier
    } make {
        ForwardStrategyImpl()
    }

    fun addKnownPeer(id: Id, hash: Long) {
        knownPeerIds.add(id)
        hashes[Key(id)] = listOf(hash)
    }

    fun addKnownPeerForHash(hash: Long) {
        addKnownPeer(idForHash[hash]!!, hash)
    }
}
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

import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.configuration
import straightway.peerspace.net.keyHasher
import straightway.peerspace.net.localPeerId
import straightway.peerspace.net.peerDirectory
import straightway.peerspace.net.timeProvider
import straightway.units.minus
import java.lang.Math.abs
import java.lang.Math.max
import java.time.LocalDateTime

/**
 * Implementation of the forward strategy for queries and pushes.
 */
class ForwardStrategyImpl : ForwardStrategy, PeerComponent by PeerComponent() {

    override fun getForwardPeerIdsFor(item: KeyHashable, state: ForwardState): Set<Id> {
        handleFailedPeers(state)
        return item.hashes.flatMap {
            peersNearerTo(it).notCoveredBy(state).take(state.numberOfReceiversToFillUp)
        }.toSet()
    }

    private fun handleFailedPeers(state: ForwardState) {
        reConsiderOldFailedPeers()
        registerFailedPeersFrom(state)
    }

    private fun registerFailedPeersFrom(state: ForwardState) =
        state.failed.forEach { failedPeers += it to timeProvider.now }

    private fun reConsiderOldFailedPeers() {
        val timeout = timeProvider.now - configuration.failedPeerIgnoreTimeout
        failedPeers = failedPeers.filter { timeout < it.value }
    }

    private val ForwardState.numberOfReceiversToFillUp get() =
            max(0, configuration.numberOfForwardPeers - nonFailed.size)

    private fun Iterable<Id>.notCoveredBy(state: ForwardState) =
            filter { it !in state.allPeerIds && it !in failedPeers }

    private fun peersNearerTo(itemHash: Long): Iterable<Id> {
        val ownDistance = abs(itemHash - ownHash)
        return peerDirectory.allKnownPeersIds.filter { otherPeer ->
            otherPeer distanceTo itemHash < ownDistance
        }.sortedBy {
            abs(itemHash - it.hash)
        }
    }

    private infix fun Id.distanceTo(chunkHash: Long) = abs(chunkHash - hash)
    private val Id.hash get() = Key(this).hashes.single()
    private val KeyHashable.hashes get() = keyHasher.getHashes(this)
    private val ForwardState.allPeerIds get() = pending + successful + failed
    private val ForwardState.nonFailed get() = pending + successful

    private val ownHash by lazy { localPeerId.hash }
    private var failedPeers = mapOf<Id, LocalDateTime>()
}
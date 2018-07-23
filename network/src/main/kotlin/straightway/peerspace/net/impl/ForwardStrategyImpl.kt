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
@file:Suppress("MatchingDeclarationName", "ForbiddenComment")

package straightway.peerspace.net.impl

import straightway.koinutils.Bean.inject
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.PeerDirectory
import straightway.random.Chooser
import java.lang.Math.abs

// TODO:
// * Avoid routing loops:
// ** Don't push the same chunk twice to the same peer (within a certain time)

/**
 * Implementation of the forward strategy for queries and pushes.
 */
class ForwardStrategyImpl : ForwardStrategy, KoinModuleComponent by KoinModuleComponent() {

    private val id: Id by property("peerId") { Id(it) }
    private val peerDirectory: PeerDirectory by inject()
    private val hasher: KeyHasher by inject()
    private val forwardPeerChooser: Chooser by inject("forwardPeerChooser")
    private val configuration: Configuration by inject()

    override fun getForwardPeerIdsFor(item: KeyHashable, state: ForwardState): Set<Id> {
        val itemsToFillUp = state.itemsToFillUp
        if (itemsToFillUp <= 0) return setOf()
        val forwardCandidates = peersNearerTo(item.hashes.single()).notCoveredBy(state)
        return forwardPeerChooser.chooseFrom(forwardCandidates, itemsToFillUp).toSet()
    }

    private val ForwardState.itemsToFillUp get() =
            configuration.numberOfForwardPeers - nonFailed.size

    private fun List<Id>.notCoveredBy(state: ForwardState) =
            filter { it !in state.allPeerIds }

    private fun peersNearerTo(itemHash: Long): List<Id> {
        val ownDistance = abs(itemHash - ownHash)
        return peerDirectory.allKnownPeersIds.filter { otherPeer ->
            otherPeer distanceTo itemHash < ownDistance
        }
    }

    private infix fun Id.distanceTo(chunkHash: Long) = abs(chunkHash - hash)
    private val Id.hash get() = Key(this).hashes.single()
    private val KeyHashable.hashes get() = hasher.getHashes(this)
    private val ForwardState.allPeerIds get() = pending + successful + failed
    private val ForwardState.nonFailed get() = pending + successful

    private val ownHash by lazy { id.hash }
}
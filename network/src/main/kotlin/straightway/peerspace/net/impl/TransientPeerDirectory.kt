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
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.configuration
import straightway.peerspace.net.timeProvider
import straightway.units.plus
import java.time.LocalDateTime

/**
 * PeerDirectory implementation holding the given peer IDs transiently.
 */
class TransientPeerDirectory : PeerDirectory, PeerComponent by PeerComponent() {

    override val allKnownPeersIds: Set<Id> get() {
        resumeUnreachablePeersIfNeeded()
        return ids.toSet() - unreachablePeerSuspensions.keys
    }

    override fun add(id: Id) {
        if (id in unreachablePeerSuspensions) return
        ids -= id
        ids += id
        cleanUpIfMaxSizeIsReached()
    }

    override fun setUnreachable(id: Id) {
        ids -= id
        ids = unreachablePeers + id + reachablePeers
        unreachablePeerSuspensions +=
                Pair(id, timeProvider.now + configuration.unreachablePeerSuspendTime)
    }

    // region Private

    private val unreachablePeers get() = ids.takeWhile { it in unreachablePeerSuspensions }
    private val reachablePeers get() = ids.dropWhile { it in unreachablePeerSuspensions }

    private fun resumeUnreachablePeersIfNeeded() {
        val now = timeProvider.now
        unreachablePeerSuspensions -= unreachablePeerSuspensions.filter { it.value <= now }.keys
    }

    private fun cleanUpIfMaxSizeIsReached() {
        if (configuration.maxKnownPeers < ids.size) {
            unreachablePeerSuspensions -= ids.first()
            ids = ids.drop(1)
        }
    }

    private var ids = listOf<Id>()
    private var unreachablePeerSuspensions = mapOf<Id, LocalDateTime>()

    // endregion
}
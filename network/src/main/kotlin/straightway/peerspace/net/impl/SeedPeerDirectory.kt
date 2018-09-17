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

/**
 * PeerDirectory implementation which delegates all actions to a
 * wrapped instance, except for a configured set of seed peers.
 * If the wrapped directory does not contain any peers, this
 * set of seed peers is returned.
 */
class SeedPeerDirectory(
        private val wrapped: PeerDirectory
) : PeerDirectory, PeerComponent by PeerComponent() {

    override val allKnownPeersIds: Set<Id>
        get() = wrapped.allKnownPeersIds.let {
            if (it.isEmpty()) configuration.seedPeerIds
            else it
        }

    override fun add(id: Id) {
        if (!id.isSeedPeerId) wrapped.add(id)
    }

    override fun setUnreachable(id: Id) {
        if (!id.isSeedPeerId) wrapped.setUnreachable(id)
    }

    private val Id.isSeedPeerId get() = this in configuration.seedPeerIds
}
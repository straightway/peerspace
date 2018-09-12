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
@file:Suppress("ForbiddenComment")
package straightway.peerspace.net.impl

import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardTargetGetter
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.Request
import straightway.peerspace.net.dataQueryHandler
import straightway.peerspace.net.forwardStrategy

/**
 * ForwardTargetGetter implementation for pushDataChunk requests.
 */
class DataPushForwardTargetGetter :
        ForwardTargetGetter,
        PeerComponent by PeerComponent() {

    override fun getForwardPeerIdsFor(item: Request<*>, state: ForwardState) =
            item.getForwardPeersFromStrategies(state) - item.remotePeerId

    private fun Request<*>.getForwardPeersFromStrategies(forwardState: ForwardState) =
            (getPushForwardPeerIds(forwardState) + content.key.queryForwardPeerIds).toSet()

    private fun Request<*>.getPushForwardPeerIds(forwardState: ForwardState) =
            forwardStrategy.getForwardPeerIdsFor(content.key, forwardState)

    private val Transmittable.key get() = (this as DataChunk).key

    private val Key.queryForwardPeerIds: Iterable<Id>
        get() = dataQueryHandler.getForwardPeerIdsFor(this)
}
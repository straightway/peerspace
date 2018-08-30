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
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.DataChunk
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Request

/**
 * Forwarder implementation for pushDataChunk requests.
 */
class DataPushForwarder :
        Forwarder<DataChunk>,
        KoinModuleComponent by KoinModuleComponent() {

    private val dataQueryHandler: DataQueryHandler by inject("dataQueryHandler")
    private val forwardStrategy: ForwardStrategy by inject()

    override fun getForwardPeerIdsFor(item: Request<DataChunk>, state: ForwardState) =
            item.getForwardPeersFromStrategies(state) - item.remotePeerId

    private fun Request<DataChunk>.getForwardPeersFromStrategies(forwardState: ForwardState) =
            (getPushForwardPeerIds(forwardState) + content.key.queryForwardPeerIds).toSet()

    private fun Request<DataChunk>.getPushForwardPeerIds(forwardState: ForwardState) =
            forwardStrategy.getForwardPeerIdsFor(content.key, forwardState)

    private val Key.queryForwardPeerIds: Iterable<Id>
        get() = dataQueryHandler.getForwardPeerIdsFor(this)
}
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
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.TransmissionResultListener

/**
 * Forwarder implementation for push requests.
 */
class PushForwarder :
        Forwarder<PushRequest, Key>,
        KoinModuleComponent by KoinModuleComponent() {

    private val peerId: Id by property("peerId") { Id(it) }
    private val network: Network by inject()
    private val dataQueryHandler: DataQueryHandler by inject("dataQueryHandler")
    private val forwardStrategy: ForwardStrategy by inject()

    override fun getKeyFor(item: PushRequest) = item.chunk.key

    override fun getForwardPeerIdsFor(item: PushRequest, state: ForwardState) =
            item.getForwardPeersFromStrategies(state) - item.originatorId

    override fun forwardTo(
            target: Id,
            item: PushRequest,
            transmissionResultListener: TransmissionResultListener
    ) =
            network.getPushTarget(target).push(
                    PushRequest(peerId, item.chunk), transmissionResultListener)

    private fun PushRequest.getForwardPeersFromStrategies(forwardState: ForwardState) =
            (getPushForwardPeerIds(forwardState) + chunk.key.queryForwardPeerIds).toSet()

    private fun PushRequest.getPushForwardPeerIds(forwardState: ForwardState) =
            forwardStrategy.getPushForwardPeerIdsFor(chunk.key, forwardState)

    private val Key.queryForwardPeerIds: Iterable<Id>
        get() = dataQueryHandler.getForwardPeerIdsFor(this)
}
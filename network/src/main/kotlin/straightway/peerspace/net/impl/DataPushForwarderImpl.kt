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

import straightway.peerspace.data.Key
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Bean.inject
import straightway.peerspace.data.isUntimed
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PushRequest

/**
 * Push data to a target peer.
 */
class DataPushForwarderImpl :
        DataPushForwarder,
        KoinModuleComponent by KoinModuleComponent() {

    private val dataQueryHandler: DataQueryHandler by inject("dataQueryHandler")
    private val forwardTracker: ForwardStateTracker<PushRequest, Key>
            by inject("pushForwardTracker")
    private val epochAnalyzer: EpochAnalyzer by inject()

    override fun forward(push: PushRequest) {

        if (push.chunk.key.isUntimed) {
            forwardTracker.forward(push)
        } else {
            push.epochs.forEach { forwardTracker.forward(push.withEpoch(it)) }
        }

        notifyForwarded(push)
    }

    private val PushRequest.epochs get() =
            epochAnalyzer.getEpochs(chunk.key.timestamps)

    private fun notifyForwarded(push: PushRequest) {
        val chunkKey = push.chunk.key
        dataQueryHandler.notifyChunkForwarded(chunkKey)
    }
}
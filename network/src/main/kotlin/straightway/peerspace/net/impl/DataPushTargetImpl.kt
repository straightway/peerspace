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

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.isUntimed
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.TransmissionResultListener

/**
 * Default implementation of the DataPushTarget interface.
 */
class DataPushTargetImpl : DataPushTarget, KoinModuleComponent by KoinModuleComponent() {

    private val peerDirectory: PeerDirectory by inject()
    private val dataChunkStore: DataChunkStore by inject()
    private val network: Network by inject()
    private val dataQueryHandler: DataQueryHandler by inject("dataQueryHandler")
    private val forwardTracker: ForwardStateTracker<DataPushRequest>
            by inject("pushForwardTracker")
    private val epochAnalyzer: EpochAnalyzer by inject()

    override fun push(
            request: DataPushRequest,
            resultListener: TransmissionResultListener
    ) {
        peerDirectory.add(request.originatorId)
        dataChunkStore.store(request.chunk)
        forward(request)
        resultListener.notifySuccess()
    }

    private fun forward(push: DataPushRequest) {
        if (push.chunk.key.isUntimed) {
            forwardTracker.forward(push)
        } else {
            push.epochs.forEach { forwardTracker.forward(push.withEpoch(it)) }
        }

        notifyForwarded(push)
        network.executePendingRequests()
    }

    private val DataPushRequest.epochs get() =
        epochAnalyzer.getEpochs(chunk.key.timestamps)

    private fun notifyForwarded(push: DataPushRequest) {
        val chunkKey = push.chunk.key
        dataQueryHandler.notifyChunkForwarded(chunkKey)
    }
}
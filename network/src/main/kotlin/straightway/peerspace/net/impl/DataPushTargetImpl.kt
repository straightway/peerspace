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

import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.isUntimed
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.Request
import straightway.peerspace.net.dataChunkStore
import straightway.peerspace.net.dataQueryHandler
import straightway.peerspace.net.epochAnalyzer
import straightway.peerspace.net.network
import straightway.peerspace.net.peerDirectory
import straightway.peerspace.net.pushForwarder

/**
 * Default implementation of the DataPushTarget interface.
 */
class DataPushTargetImpl : DataPushTarget, KoinModuleComponent by KoinModuleComponent() {

    override fun pushDataChunk(request: Request<DataChunk>) {
        peerDirectory.add(request.remotePeerId)
        dataChunkStore.store(request.content)
        forward(request)
    }

    private fun forward(request: Request<DataChunk>) {
        if (request.content.key.isUntimed) {
            pushForwarder.forward(request)
        } else {
            request.content.epochs.forEach {
                pushForwarder.forward(
                        Request(request.remotePeerId, request.content.withEpoch(it)))
            }
        }

        notifyForwarded(request.content)
        network.executePendingRequests()
    }

    private val DataChunk.epochs get() =
        epochAnalyzer.getEpochs(key.timestamps)

    private fun notifyForwarded(data: DataChunk) {
        val chunkKey = data.key
        dataQueryHandler.notifyChunkForwarded(chunkKey)
    }
}
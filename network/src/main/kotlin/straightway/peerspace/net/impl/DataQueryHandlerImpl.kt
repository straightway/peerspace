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
import straightway.peerspace.data.isUntimed
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.isUntimed

/**
 * Handle timed and untimed data queries.
 */
class DataQueryHandlerImpl : DataQueryHandler, KoinModuleComponent by KoinModuleComponent() {

    private val untimedDataQueryHandler by inject<DataQueryHandler>("untimedDataQueryHandler")
    private val timedDataQueryHandler by inject<DataQueryHandler>("timedDataQueryHandler")

    override fun handle(query: QueryRequest) {
        if (query.isUntimed) untimedDataQueryHandler.handle(query)
        else timedDataQueryHandler.handle(query)
    }

    override fun getForwardPeerIdsFor(chunkKey: Key) =
            (untimedDataQueryHandler.getForwardPeerIdsFor(chunkKey) +
             timedDataQueryHandler.getForwardPeerIdsFor(chunkKey)).toSet()

    override fun notifyChunkForwarded(key: Key) {
        if (key.isUntimed) untimedDataQueryHandler.notifyChunkForwarded(key)
        else timedDataQueryHandler.notifyChunkForwarded(key)
    }
}
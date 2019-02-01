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
@file:Suppress("UnusedImports")

package straightway.peerspace.transport.impl

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.DataQueryCallback
import straightway.peerspace.transport.ListQuery
import straightway.peerspace.transport.ListQueryCallback
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.chunker
import straightway.peerspace.transport.createDataQueryTracker
import straightway.peerspace.transport.createListQueryTracker
import straightway.peerspace.transport.peerClient
import straightway.peerspace.transport.timeProvider
import straightway.peerspace.net.ChunkQueryControl as NetQueryControl
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Default implementation of the Transport interface.
 */
class TransportImpl : Transport, TransportComponent by TransportComponent() {

    override fun store(data: ByteArray) =
            with(chunker.chopToChunks(data)) {
                forEach { peerClient.store(it) }
                first().id.id
            }

    override fun post(listId: Id, data: ByteArray) =
            with(chunker.chopToChunks(data)) {
                val key = Key(listId, currentTimeStamp, 0)
                peerClient.store(DataChunk(key, chunker.chopToChunks(data).first().data))
                drop(1).forEach { peerClient.store(it) }
            }

    override fun query(id: Id, querySetup: DataQueryCallback.() -> Unit) {
        createDataQueryTracker(id, querySetup)
    }

    override fun query(query: ListQuery, querySetup: ListQueryCallback.() -> Unit) {
        createListQueryTracker(query, querySetup)
    }

    private val currentTimeStamp get() =
        ChronoUnit.MILLIS.between(LocalDateTime.of(0, 1, 1, 0, 0), timeProvider.now)
}
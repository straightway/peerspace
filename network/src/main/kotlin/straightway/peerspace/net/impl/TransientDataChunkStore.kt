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

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.QueryRequest

/**
 * Store data chunks transiently.
 */
class TransientDataChunkStore : DataChunkStore {

    override fun store(chunk: Chunk) {
        storedData[chunk.key] = chunk
    }

    override fun query(queryRequest: QueryRequest) =
            storedData.values.filter { it satisfies queryRequest }

    operator fun get(key: Key): Chunk? = storedData[key]

    private infix fun Chunk.satisfies(queryRequest: QueryRequest) =
            key.id == queryRequest.id && key.timestamp in queryRequest.timestamps

    private val storedData = mutableMapOf<Key, Chunk>()
}
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

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Key
import straightway.peerspace.data.chunkSize
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.configuration
import straightway.units.AmountOfData
import straightway.units.UnitValue
import straightway.units.plus
import straightway.units.times

/**
 * Store data chunks transiently.
 */
class TransientDataChunkStore : DataChunkStore, PeerComponent by PeerComponent() {

    override fun store(chunk: DataChunk) {
        if (chunk.exceedsCapacity)
            dropOldestChunk()
        storeNew(chunk)
    }

    override fun query(queryRequest: DataChunkQuery) =
            storedData.values.filter {
                it satisfies queryRequest
            }.apply {
                forEach { it.markAsNew() }
            }

    operator fun get(key: Key): DataChunk? = storedData[key]

    private fun storeNew(chunk: DataChunk) {
        chunk.markAsNew()
        storedData[chunk.key] = chunk
    }

    private fun DataChunk.markAsNew() {
        storedChunkKeysInArrivalOrder -= key
        storedChunkKeysInArrivalOrder += key
    }

    private fun dropOldestChunk() {
        storedData.remove(storedChunkKeysInArrivalOrder.first())
        storedChunkKeysInArrivalOrder = storedChunkKeysInArrivalOrder.drop(1)
    }

    private val DataChunk.exceedsCapacity get() =
            configuration.storageCapacity < currentStorageSize + chunkSize

    private val currentStorageSize: UnitValue<AmountOfData> get() =
            storedData.values.size * chunkSize

    private infix fun DataChunk.satisfies(queryRequest: DataChunkQuery) =
            key.id == queryRequest.chunkId && key.timestamp in queryRequest.timestamps

    private val storedData = mutableMapOf<Key, DataChunk>()
    private var storedChunkKeysInArrivalOrder = listOf<Key>()
}
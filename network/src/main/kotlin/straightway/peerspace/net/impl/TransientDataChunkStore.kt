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
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.ChunkSizeGetter
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.QueryRequest
import straightway.units.AmountOfData
import straightway.units.UnitNumber
import straightway.units.bit
import straightway.units.get
import straightway.units.plus

/**
 * Store data chunks transiently.
 */
class TransientDataChunkStore : DataChunkStore, KoinModuleComponent by KoinModuleComponent() {

    private val configuration: Configuration by inject()
    private val chunkSizeGetter: ChunkSizeGetter by inject()

    override fun store(chunk: Chunk) {
        if (chunk.exceedsCapacity)
            dropOldestChunk()
        storeNew(chunk)
    }

    override fun query(queryRequest: QueryRequest) =
            storedData.values.filter {
                it satisfies queryRequest
            }.apply {
                forEach { it.markAsNew() }
            }

    operator fun get(key: Key): Chunk? = storedData[key]

    private fun storeNew(chunk: Chunk) {
        chunk.markAsNew()
        storedData[chunk.key] = chunk
    }

    private fun Chunk.markAsNew() {
        storedChunkKeysInArrivalOrder -= key
        storedChunkKeysInArrivalOrder += key
    }

    private fun dropOldestChunk() {
        storedData.remove(storedChunkKeysInArrivalOrder.first())
        storedChunkKeysInArrivalOrder = storedChunkKeysInArrivalOrder.drop(1)
    }

    private val Chunk.exceedsCapacity get() =
            configuration.storageCapacity < currentStorageSize + chunkSizeGetter(this)

    private val currentStorageSize: UnitNumber<AmountOfData> get() =
            storedData.values.fold(0[bit]) {
                acc: UnitNumber<AmountOfData>, chunk: Chunk ->
                acc + chunkSizeGetter(chunk)
            }

    private infix fun Chunk.satisfies(queryRequest: QueryRequest) =
            key.id == queryRequest.id && key.timestamp in queryRequest.timestamps

    private val storedData = mutableMapOf<Key, Chunk>()
    private var storedChunkKeysInArrivalOrder = listOf<Key>()
}
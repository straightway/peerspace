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
package straightway.peerspace.transport.impl

import straightway.error.Panic
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.DeChunker
import straightway.peerspace.transport.DeChunkerCrypto

/**
 * Default implementation of the DeChunker interface.
 */
class DeChunkerImpl : DeChunker {

    override fun tryCombining(
            chunks: Collection<DataChunk>,
            deChunkerCrypto: DeChunkerCrypto) =
            ChunkSetAnalyzer(chunks.map { it.decryptWith(deChunkerCrypto) }).aggregatedPayload

    override fun getReferencedChunks(
            data: ByteArray,
            deChunkerCrypto: DeChunkerCrypto) = DataChunkStructure.fromBinary(data).references

    private fun DataChunk.decryptWith(deChunkerCrypto: DeChunkerCrypto) =
            DataChunk(key, decryptPayloadWith(deChunkerCrypto))

    @Suppress("UNUSED_PARAMETER")
    private fun DataChunk.decryptPayloadWith(deChunkerCrypto: DeChunkerCrypto) =
            deChunkerCrypto.decryptor.decrypt(DataChunkStructure.fromBinary(data).payload)

    private class ChunkSetAnalyzer(rawChunks: Collection<DataChunk>) {
        val aggregatedPayload: ByteArray? get() = rootKey?.aggregatedPayload

        private val Key.aggregatedPayload: ByteArray? get() = preventLoops {
            references.map { it.aggregatedPayload }.fold(payload) { acc, item -> acc?.plus(item) }
        }

        private fun ByteArray.plus(a: ByteArray?): ByteArray? =
                if (a == null) null else this + a

        private fun<T> Key.preventLoops(action: () -> T): T {
            if (this in referenceParents) throw Panic("recursive chunk structure")
            referenceParents += this
            try {
                return action()
            }
            finally {
                referenceParents -= this
            }
        }

        private val Key?.payload get() = allChunks[this]?.payload

        private fun addReferencesFromChunk(chunkKey: Key, chunk: DataChunkStructure) =
                chunk.references.keys.forEach { chunkKey references it }

        private val List<Id>.keys get() = map { Key(it) }

        private infix fun Key.references(referencedKey: Key) {
            references += referencedKey
            referencedKeys += referencedKey
        }

        private var Key.references: List<Key>
            get() = this@ChunkSetAnalyzer.references[this] ?: listOf()
            set(value) { this@ChunkSetAnalyzer.references[this] = value }

        private val rootKey by lazy { rootKeys.singleOrNull() }

        private val rootKeys get() = allChunks.keys - referencedKeys

        private val allChunks =
                rawChunks.map { it.key to DataChunkStructure.fromBinary(it.data) }.toMap()

        private val references = mutableMapOf<Key, List<Key>>()

        private val referencedKeys = mutableSetOf<Key>()

        private val referenceParents = mutableSetOf<Key>()

        init {
            allChunks.forEach(::addReferencesFromChunk)
        }
    }
}

private val DataChunkStructure.references get() =
        controlBlocks
                .filter { it.type == DataChunkControlBlockType.ReferencedChunk }
                .map { Id(it.content) }

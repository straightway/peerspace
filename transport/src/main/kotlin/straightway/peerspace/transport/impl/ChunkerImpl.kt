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

import straightway.koinutils.Bean.get
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkVersion2Builder
import straightway.peerspace.data.DataChunkStructure
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        private val chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    private val hasher get() = get<Hasher>()

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto): Set<DataChunk> {
        return when {
            data.size == version0PayloadSize ->
                singleChunk(DataChunkStructure.version0(data))
            data.size <= maxVersion1PayloadSize ->
                singleChunk(DataChunkVersion2Builder(chunkSizeBytes)
                        .apply { payload = data }.chunkStructure)
            else -> Chopper(data).chunks
        }
    }

    private fun singleChunk(chunk: DataChunkStructure): Set<DataChunk> {
        val chunkBinarayData = chunk.binary
        val dataHash = hasher.getHash(chunkBinarayData)
        return setOf(DataChunk(Key(Id(dataHash)), chunkBinarayData))
    }

    private inner class Chopper(data: ByteArray) {

        private var rest = data
        private val result = mutableSetOf<DataChunk>()

        val chunks get() = result

        private fun chop(maxDepth: Int, dirBuilder: DataChunkVersion2Builder): DataChunk {
            return when {
                maxDepth == 0 || dirBuilder.isFittingCompletely ->
                    createPlainChunkStructure()
                else -> {
                    println("creating directory of depth $maxDepth")
                    createDirectoryOfDepth(maxDepth, dirBuilder)
                }
            }.createChunk().apply {
                result.add(this)
            }
        }

        private fun createPlainChunkStructure(): DataChunkStructure {
            println("creating plain chunk")
            return if (version0PayloadSize <= rest.size)
                DataChunkStructure.version0(consume(version0PayloadSize))
            else
                DataChunkVersion2Builder(chunkSizeBytes)
                        .apply { payload = consume(chunkSizeBytes) }.chunkStructure
        }

        private val DataChunkVersion2Builder.isFittingCompletely get() =
            rest.size <= (version0PayloadSize + availablePayloadBytes)

        private fun createDirectoryOfDepth(
                maxDepth: Int,
                dirBuilder: DataChunkVersion2Builder
        ): DataChunkStructure {
            val directory = DataChunkVersion2Builder(chunkSizeBytes)
            while (directory.needsAnotherReference) {
                directory.references += hasher.getHash(chop(maxDepth - 1, dirBuilder).data)
                println("adding reference ${Id(directory.references.last())}")
            }
            rest = directory.setPayloadPart(rest)
            return directory.chunkStructure
        }

        private val DataChunkVersion2Builder.needsAnotherReference get() =
            availablePayloadBytes < rest.size && references.size < maxReferences

        private fun consume(numberOfBytes: Int): ByteArray =
                if (numberOfBytes <= rest.size)
                    rest.sliceArray(0 until numberOfBytes).apply {
                        rest = rest.sliceArray(numberOfBytes..rest.lastIndex)
                    }
                else rest.clone().apply { rest = byteArrayOf() }

        private fun DataChunkStructure.createChunk() =
                createChunk(Key(Id(hasher.getHash(binary)))).apply {
                    println("created chunk $key: ${this@createChunk}")
                }

        init {
            var currentDepth = 0
            var rootDirectory = DataChunkVersion2Builder(chunkSizeBytes)
            while (true) {
                while (rootDirectory.needsAnotherReference) {
                    rootDirectory.references +=
                            hasher.getHash(chop(currentDepth, rootDirectory).data)
                    println("ctor: adding reference ${Id(rootDirectory.references.last())}")
                }
                rest = rootDirectory.setPayloadPart(rest)
                val rootDirChunk = rootDirectory.chunkStructure.createChunk()
                result.add(rootDirChunk)
                if (rest.any()) {
                    rootDirectory = DataChunkVersion2Builder(chunkSizeBytes)
                    rootDirectory.references += hasher.getHash(rootDirChunk.data)
                    ++currentDepth
                    println("new root, depth = $currentDepth")
                } else {
                    println("finished")
                    break
                }
            }

            val rootDirBinary = rootDirectory.chunkStructure.binary
            val rootDirHash = hasher.getHash(rootDirBinary)
            chunks.add(rootDirectory.chunkStructure.createChunk(Key(Id(rootDirHash))))
        }
    }

    private val maxVersion1PayloadSize =
            chunkSizeBytes - DataChunkStructure.Header.Version2.MIN_SIZE
    private val version0PayloadSize =
            chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE
}
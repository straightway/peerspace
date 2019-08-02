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

/**
 * Determine the depth and structure of a tree of data chunks.
 */
class ChunkTreeDepthInfo private constructor(
        private val depth: Int,
        val maxSubTreeSize: Int,
        private val chunkProperties: ChunkProperties
) {

    constructor(chunkProperties: ChunkProperties) : this(0, 0, chunkProperties)

    fun getMinimumForSize(dataSize: Int): ChunkTreeDepthInfo =
            if (dataSize <= maxSize) this@ChunkTreeDepthInfo
            else ChunkTreeDepthInfo(depth + 1, maxSize, chunkProperties)
                    .getMinimumForSize(dataSize)

    fun getDirectoryPayloadSize(aggregatedPayloadSize: Int) =
            chunkProperties.getMaxChunkVersion2PayloadSizeWithReferences(
                    getNumberOfReferencesForSize(aggregatedPayloadSize))

    private fun getNumberOfReferencesForSize(dataSize: Int): Int =
        (0..chunkProperties.maxReferences).first { dataSize <= getSizeForReferences(it) }

    private fun getSizeForReferences(references: Int): Int =
            references * maxSubTreeSize + chunkProperties.getMaxChunkVersion2PayloadSizeWithReferences(references)

    private val maxSize =
            if (depth <= 0) chunkProperties.version0PayloadSizeInCryptoContainer
            else getSizeForReferences(chunkProperties.maxReferences)
}

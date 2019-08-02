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
 * Values for determining the structure of data chunks when splitting data.
 */
data class ChunkProperties(
        val chunkSizeBytes: Int,
        val maxReferences: Int,
        val referenceBlockSizeBytes: Int,
        val dataBlockSizeBytes: Int,
        val cryptoContainerHeaderSizeBytes: Int)
{
    constructor(chunkSizeBytes: Int, maxReferences: Int, referenceBlockSize: Int, blockSizeBytes: Int)
            : this(chunkSizeBytes, maxReferences, referenceBlockSize, blockSizeBytes, DataChunkVersion0.Header.SIZE)
}

val ChunkProperties.cryptoContainerPayloadSizeBytes get() =
    chunkSizeBytes - cryptoContainerHeaderSizeBytes

val ChunkProperties.chunkSizeBytesRespectingCryptoDataBlocks get() =
    (cryptoContainerPayloadSizeBytes / dataBlockSizeBytes) * dataBlockSizeBytes

val ChunkProperties.maxVersion2PayloadSize get() =
    chunkSizeBytesRespectingCryptoDataBlocks - DataChunkVersion2.Header.MIN_SIZE

val ChunkProperties.version0PayloadSizeInCryptoContainer get() =
    chunkSizeBytesRespectingCryptoDataBlocks - DataChunkVersion0.Header.SIZE

fun ChunkProperties.getMaxChunkVersion2PayloadSizeWithReferences(numberOfReferences: Int) =
        chunkSizeBytesRespectingCryptoDataBlocks - DataChunkVersion2.Header.MIN_SIZE -
                numberOfReferences * referenceBlockSizeBytes

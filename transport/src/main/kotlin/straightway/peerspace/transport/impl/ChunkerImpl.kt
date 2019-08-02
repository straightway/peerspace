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

import straightway.koinutils.withOwnContext
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.crypto.hashBytes
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.createHasher
import straightway.peerspace.transport.tracer

/**
 * Default implementation of the Chunker interface.
 */
class ChunkerImpl(
        private val chunkSizeBytes: Int,
        private val maxReferences: Int
) : Chunker, TransportComponent by TransportComponent() {

    override fun chopToChunks(data: ByteArray, crypto: ChunkerCrypto) = tracer(data, crypto) {
        val hasher = createHasher()
        val chunkProperties =
            ChunkProperties(
                chunkSizeBytes,
                maxReferences,
                hasher.referenceBlockSize,
                crypto.encryptor.encryptorProperties.blockBytes)
        val chopper = withOwnContext { ChunkTreeCreator(data, crypto, hasher, chunkProperties) }
        chopper.chunks
    }
}

private val Hasher.referenceBlockSize get() = DataChunkControlBlock.NON_CONTENT_SIZE + hashBytes
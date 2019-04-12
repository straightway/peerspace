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

open class ChunkEnvironmentValues(
        val chunkSizeBytes: Int,
        val cryptoBlockSize: Int) {

    companion object {
        const val HASH_BITS = Int.SIZE_BITS
    }

    val encryptedPayloadSizeBytes get() =
            chunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

    val unencryptedChunkSizeBytes get() =
            (encryptedPayloadSizeBytes / cryptoBlockSize) * cryptoBlockSize

    val payloadBytesVersion0 =
            unencryptedChunkSizeBytes - DataChunkStructure.Header.Version0.SIZE

    val maxPayloadBytesVersion1 =
            unencryptedChunkSizeBytes - DataChunkStructure.Header.Version1.SIZE

    fun maxChunkVersion2PayloadSizeWithReferences(numberOfReferences: Int) =
            unencryptedChunkSizeBytes -
                    DataChunkStructure.Header.Version2.MIN_SIZE -
                    numberOfReferences * referenceBlockSize

    private val referenceBlockSize get() =
            DataChunkControlBlock.NON_CONTENT_SIZE + (HASH_BITS - 1) / Byte.SIZE_BITS + 1
}
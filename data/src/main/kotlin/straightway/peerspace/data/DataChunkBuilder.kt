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
package straightway.peerspace.data

/**
 * Builder class for data chunks (via DataChunkStructure).
 */
class DataChunkBuilder private constructor() {

    companion object {
        operator fun invoke(init: DataChunkBuilder.() -> Unit) =
                DataChunkBuilder().run {
                    init()
                    getDataChunkStructure(controlBlocks)
                }
    }

    var signMode: DataChunkSignMode = DataChunkSignMode.NoKey
    var signature: ByteArray? = null
    var publicKey: ByteArray? = null
    var contentKey: ByteArray? = null
    var references = listOf<ByteArray>()
    var payload = byteArrayOf()
    val signablePart get() = getDataChunkStructure(signableBlocks).binary.allBytesExceptVersion

    // region Private

    private fun getDataChunkStructure(blocks: List<DataChunkControlBlock>) =
            DataChunkStructure(blocks, payload)
    private val controlBlocks get() = signatureBlock + signableBlocks
    private val signableBlocks get() = publicKeyBlock + contentKeyBlock + referenceBlocks
    private val signatureBlock get() =
        createControlBlock(DataChunkControlBlockType.Signature, signature, signMode.id)
    private val publicKeyBlock get() =
            createControlBlock(DataChunkControlBlockType.PublicKey, publicKey)
    private val contentKeyBlock get() =
            createControlBlock(DataChunkControlBlockType.ContentKey, contentKey)
    private val referenceBlocks get() = references.map {
        DataChunkControlBlock(DataChunkControlBlockType.ReferencedChunk, 0, it)
    }
    @Suppress("LongParameterList")
    private fun createControlBlock(
            type: DataChunkControlBlockType, content: ByteArray?, cpls: Byte = 0
    ) =
            if (content == null) listOf() else listOf(DataChunkControlBlock(type, cpls, content))
    private val ByteArray.allBytesExceptVersion get() = sliceArray(1..lastIndex)

    // endregion
}

fun createDataChunk(key: Key, init: DataChunkBuilder.() -> Unit) =
        DataChunkBuilder(init).createChunk(key)
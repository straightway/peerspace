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

import org.junit.jupiter.api.Test
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Null
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkVersion2BuilderTest {

    private companion object {
        const val testChunkSize = 0xffff
    }

    @Test
    fun `version is 2`() =
            expect(DataChunkVersion2Builder.VERSION is_ Equal to_ 2)

    @Test
    fun `signMode is initially NoKey`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            expect(signMode is_ Equal to_ DataChunkSignMode.NoKey)
        }
    }

    @Test
    fun `initial signature is null`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(signature is_ Null) }
    }

    @Test
    fun `initial publicKey is not accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(publicKey is_ Null) }
    }

    @Test
    fun `initial contentKey is not accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(contentKey is_ Null) }
    }

    @Test
    fun `initial references are empty`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(references is_ Empty) }
    }

    @Test
    fun `initial payload is empty`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(payload is_ Empty) }
    }

    @Test
    fun `chunk data structure of initial state has version 2`() {
        val result = DataChunkVersion2Builder(testChunkSize).chunkStructure
        expect(result.version is_ Equal to_ 2)
    }

    @Test
    fun `chunk data structure of initial state has empty payload`() {
        val result = DataChunkVersion2Builder(testChunkSize)
        expect(result.payload is_ Empty)
    }

    @Test
    fun `chunk data structure of contains set payload`() {
        val fullPayload = ByteArray(CHUNK_SIZE_BYTES - 5) { it.toByte() }
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            payload = fullPayload
        }
        expect(result.payload is_ Equal to_ fullPayload)
    }

    @Test
    fun `set signature is accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            signature = byteArrayOf(1, 2, 3)
            expect(signature is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set signature results in signature control block`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            signature = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().type is_ Equal to_ DataChunkControlBlockType.Signature)
    }

    @Test
    fun `set signature results in control block with signature as content`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            signature = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set signature after signMode results in control block with signMode as cpls`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            signMode = DataChunkSignMode.EmbeddedKey
            signature = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().cpls is_ Equal to_ DataChunkSignMode.EmbeddedKey.id)
    }

    @Test
    fun `set signMode after signature results in control block with signMode as cpls`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            signature = byteArrayOf(1, 2, 3)
            signMode = DataChunkSignMode.EmbeddedKey
        }.chunkStructure
        expect(result.controlBlocks.first().cpls is_ Equal to_ DataChunkSignMode.EmbeddedKey.id)
    }

    @Test
    fun `set signature null does not throw`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            signature = null
            expect(signature is_ Null)
        }
    }

    @Test
    fun `set publicKey is accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            publicKey = byteArrayOf(1, 2, 3)
            expect(publicKey is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set publicKey null does not throw`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            publicKey = null
            expect(publicKey is_ Null)
        }
    }

    @Test
    fun `set publicKey results in public key control block`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            publicKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().type is_ Equal to_ DataChunkControlBlockType.PublicKey)
    }

    @Test
    fun `set publicKey results in control block with publicKey as content`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            publicKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set publicKey results in control block with cpls 0`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            publicKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().cpls is_ Equal to_ 0)
    }

    @Test
    fun `set contentKey is accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = byteArrayOf(1, 2, 3)
            expect(contentKey is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set contentKey null does not throw`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = null
            expect(publicKey is_ Null)
        }
    }

    @Test
    fun `set contentKey results in content key control block`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().type is_ Equal
                to_ DataChunkControlBlockType.ContentKey)
    }

    @Test
    fun `set contentKey results in control block with contentKey as content`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set contentKey results in control block with cpls 0`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().cpls is_ Equal to_ 0)
    }

    @Test
    fun `added reference is accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            references += byteArrayOf(1, 2, 3)
            expect(references is_ Equal to_ listOf(byteArrayOf(1, 2, 3)))
        }
    }

    @Test
    fun `add reference results in reference control block`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            references += byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().type is_ Equal
                to_ DataChunkControlBlockType.ReferencedChunk)
    }

    @Test
    fun `add reference results in control block with reference as content`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            references += byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `add reference results in control block with cpls 0`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            references += byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.first().cpls is_ Equal to_ 0)
    }

    @Test
    fun `order of control blocks is as expected`() {
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            references += byteArrayOf(1, 2, 3)
            contentKey = byteArrayOf(1, 2, 3)
            signature = byteArrayOf(1, 2, 3)
            publicKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.map { it.type } is_ Equal to_ listOf(
                DataChunkControlBlockType.Signature,
                DataChunkControlBlockType.PublicKey,
                DataChunkControlBlockType.ContentKey,
                DataChunkControlBlockType.ReferencedChunk,
                DataChunkControlBlockType.ReferencedChunk))
    }

    @Test
    fun `signablePart contains everything after the signature`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            signMode = DataChunkSignMode.EmbeddedKey
            signature = byteArrayOf(1)
            publicKey = byteArrayOf(2)
            contentKey = byteArrayOf(3)
            references += byteArrayOf(4)
            references += byteArrayOf(5)
            payload = byteArrayOf(6)
            expect(signablePart is_ Equal to_ byteArrayOf(
                    0x02, 0x00, 0x01, 0x02, // public key
                    0x03, 0x00, 0x01, 0x03, // content key
                    0x04, 0x00, 0x01, 0x04, // reference to 4
                    0x04, 0x00, 0x01, 0x05, // reference to 5
                    DataChunkStructure.Header.Version2.CEND,
                    0x00, 0x01,             // payload size
                    6))                     // payload
        }
    }

    @Test
    fun `setPayloadPart for completely fitting payload adds full payload`() {
        val fullPayload = ByteArray(testChunkSize - 4) { it.toByte() }
        DataChunkVersion2Builder(testChunkSize).apply {
            setPayloadPart(fullPayload)
            expect(payload is_ Equal to_ fullPayload)
        }
    }

    @Test
    fun `setPayloadPart for partly fitting payload adds fitting part from start`() {
        val fullPayload = ByteArray(testChunkSize) { it.toByte() }
        DataChunkVersion2Builder(testChunkSize).apply {
            val rest = setPayloadPart(fullPayload)
            val expectedPayloadSize = fullPayload.size - DataChunkStructure.Header.Version2.MIN_SIZE
            expect(payload is_ Equal
                    to_ fullPayload.sliceArray(0 until expectedPayloadSize))
            expect(rest is_ Equal
                    to_ fullPayload.sliceArray(expectedPayloadSize..fullPayload.lastIndex))
        }
    }

    @Test
    fun `setPayloadPart for partly fitting payload with control blocks`() {
        val fullPayload = ByteArray(testChunkSize) { it.toByte() }
        DataChunkVersion2Builder(testChunkSize).apply {
            contentKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(4, 5, 6)
            val rest = setPayloadPart(fullPayload)
            expect(availableBytes is_ Equal to_ 0)
            expect(payload is_ Equal to_ fullPayload.sliceArray(0 until availablePayloadBytes))
            expect(rest is_ Equal to_
                    fullPayload.sliceArray(availablePayloadBytes..fullPayload.lastIndex))
        }
    }

    @Test
    fun `setPayloadPart for completely fitting payload returns empty array`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            expect(setPayloadPart(byteArrayOf(1, 2, 3)) is_ Empty)
        }
    }

    @Test
    fun `availableBytes of empty chunk is chunkSize without header`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            expect(availableBytes is_ Equal
                    to_ chunkSize - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availableBytes considers size of control blocks`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            val referenceBlock = DataChunkControlBlock(
                    DataChunkControlBlockType.ReferencedChunk, 0x0, byteArrayOf(1, 2, 3))
            references += referenceBlock.content
            expect(availableBytes is_ Equal
                    to_ chunkSize - referenceBlock.binarySize
                    - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availableBytes considers size of payload`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            payload = byteArrayOf(1, 2, 3)
            expect(availableBytes is_ Equal
                    to_ chunkSize - payload.size - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availableBytes changes with the chunk size`() {
        DataChunkVersion2Builder(16).apply {
            expect(availableBytes is_ Equal
                    to_ chunkSize - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes of empty chunk is chunkSize without header`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes considers size of control blocks`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            val referenceBlock = DataChunkControlBlock(
                    DataChunkControlBlockType.ReferencedChunk, 0x0, byteArrayOf(1, 2, 3))
            references += referenceBlock.content
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - referenceBlock.binarySize
                    - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes ignores size of payload`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            payload = byteArrayOf(1, 2, 3)
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes changes with the chunk size`() {
        DataChunkVersion2Builder(16).apply {
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkStructure.Header.Version2.MIN_SIZE)
        }
    }
}
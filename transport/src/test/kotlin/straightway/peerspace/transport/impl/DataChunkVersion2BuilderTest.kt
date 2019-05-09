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
    fun `initial publicKey is not accessible`() {
        DataChunkVersion2Builder(testChunkSize).apply { expect(publicKey is_ Null) }
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
        val fullPayload = ByteArray(testChunkSize - 5) { it.toByte() }
        val result = DataChunkVersion2Builder(testChunkSize).apply {
            payload = fullPayload
        }
        expect(result.payload is_ Equal to_ fullPayload)
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
            publicKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(1, 2, 3)
        }.chunkStructure
        expect(result.controlBlocks.map { it.type } is_ Equal to_ listOf(
                DataChunkControlBlockType.PublicKey,
                DataChunkControlBlockType.ReferencedChunk,
                DataChunkControlBlockType.ReferencedChunk))
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
            val expectedPayloadSize = fullPayload.size - DataChunkVersion2.Header.MIN_SIZE
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
            publicKey = byteArrayOf(1, 2, 3)
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
                    to_ chunkSize - DataChunkVersion2.Header.MIN_SIZE)
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
                    - DataChunkVersion2.Header.MIN_SIZE)
        }
    }

    @Test
    fun `availableBytes considers size of payload`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            payload = byteArrayOf(1, 2, 3)
            expect(availableBytes is_ Equal to_
                    chunkSize - payload.size - DataChunkVersion2.Header.MIN_SIZE)
        }
    }

    @Test
    fun `availableBytes changes with the chunk size`() {
        DataChunkVersion2Builder(16).apply {
            expect(availableBytes is_ Equal
                    to_ chunkSize - DataChunkVersion2.Header.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes of empty chunk is chunkSize without header`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkVersion2.Header.MIN_SIZE)
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
                    - DataChunkVersion2.Header.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes ignores size of payload`() {
        DataChunkVersion2Builder(testChunkSize).apply {
            payload = byteArrayOf(1, 2, 3)
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkVersion2.Header.MIN_SIZE)
        }
    }

    @Test
    fun `availablePayloadBytes changes with the chunk size`() {
        DataChunkVersion2Builder(16).apply {
            expect(availablePayloadBytes is_ Equal
                    to_ chunkSize - DataChunkVersion2.Header.MIN_SIZE)
        }
    }
}
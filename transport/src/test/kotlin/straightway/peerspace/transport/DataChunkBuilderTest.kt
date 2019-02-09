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
package straightway.peerspace.transport

import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Null
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkBuilderTest {

    @Test
    fun `initializer is called`() {
        var isInitialized = false
        DataChunkBuilder { isInitialized = true }
        expect(isInitialized)
    }

    @Test
    fun `signMode is initially NoKey`() {
        DataChunkBuilder { expect(signMode is_ Equal to_ DataChunkSignMode.NoKey) }
    }

    @Test
    fun `initial signature is null`() {
        DataChunkBuilder { expect(signature is_ Null) }
    }

    @Test
    fun `initial publicKey is not accessible`() {
        DataChunkBuilder { expect(publicKey is_ Null) }
    }

    @Test
    fun `initial contentKey is not accessible`() {
        DataChunkBuilder { expect(contentKey is_ Null) }
    }

    @Test
    fun `initial references are empty`() {
        DataChunkBuilder { expect(references is_ Empty) }
    }

    @Test
    fun `initial payload is empty`() {
        DataChunkBuilder { expect(payload is_ Empty) }
    }

    @Test
    fun `chunk data structure of initial state has version 0`() {
        val result = DataChunkBuilder {}
        expect(result.version is_ Equal to_ 0)
    }

    @Test
    fun `chunk data structure of initial state has no control blocks`() {
        val result = DataChunkBuilder {}
        expect(result.controlBlocks is_ Empty)
    }

    @Test
    fun `chunk data structure of initial state has empty payload`() {
        val result = DataChunkBuilder {}
        expect(result.payload is_ Empty)
    }

    @Test
    fun `chunk data structure of contains set payload`() {
        val result = DataChunkBuilder {
            payload = byteArrayOf(1, 2, 3)
        }
        expect(result.payload is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set signature is accessible`() {
        DataChunkBuilder {
            signature = byteArrayOf(1, 2, 3)
            expect(signature is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set signature results in signature control block`() {
        val result = DataChunkBuilder {
            signature = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().type is_ Equal to_ DataChunkControlBlockType.Signature)
    }

    @Test
    fun `set signature results in control block with signature as content`() {
        val result = DataChunkBuilder {
            signature = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set signature after signMode results in control block with signMode as cpls`() {
        val result = DataChunkBuilder {
            signMode = DataChunkSignMode.EmbeddedKey
            signature = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().cpls is_ Equal to_ DataChunkSignMode.EmbeddedKey.id)
    }

    @Test
    fun `set signMode after signature results in control block with signMode as cpls`() {
        val result = DataChunkBuilder {
            signature = byteArrayOf(1, 2, 3)
            signMode = DataChunkSignMode.EmbeddedKey
        }
        expect(result.controlBlocks.single().cpls is_ Equal to_ DataChunkSignMode.EmbeddedKey.id)
    }

    @Test
    fun `set signature null does not throw`() {
        DataChunkBuilder {
            signature = null
            expect(signature is_ Null)
        }
    }

    @Test
    fun `having a signature makes the chunk version 1`() {
        val result = DataChunkBuilder {
            signature = byteArrayOf(1, 2, 3)
        }
        expect(result.version is_ Equal to_ 1)
    }

    @Test
    fun `set publicKey is accessible`() {
        DataChunkBuilder {
            publicKey = byteArrayOf(1, 2, 3)
            expect(publicKey is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set publicKey null does not throw`() {
        DataChunkBuilder {
            publicKey = null
            expect(publicKey is_ Null)
        }
    }

    @Test
    fun `set publicKey results in public key control block`() {
        val result = DataChunkBuilder {
            publicKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().type is_ Equal to_ DataChunkControlBlockType.PublicKey)
    }

    @Test
    fun `set publicKey results in control block with publicKey as content`() {
        val result = DataChunkBuilder {
            publicKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set publicKey results in control block with cpls 0`() {
        val result = DataChunkBuilder {
            publicKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().cpls is_ Equal to_ 0)
    }

    @Test
    fun `having a publicKey makes the chunk version 1`() {
        val result = DataChunkBuilder {
            publicKey = byteArrayOf(1, 2, 3)
        }
        expect(result.version is_ Equal to_ 1)
    }

    @Test
    fun `set contentKey is accessible`() {
        DataChunkBuilder {
            contentKey = byteArrayOf(1, 2, 3)
            expect(contentKey is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `set contentKey null does not throw`() {
        DataChunkBuilder {
            contentKey = null
            expect(publicKey is_ Null)
        }
    }

    @Test
    fun `set contentKey results in content key control block`() {
        val result = DataChunkBuilder {
            contentKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().type is_ Equal
                to_ DataChunkControlBlockType.ContentKey)
    }

    @Test
    fun `set contentKey results in control block with contentKey as content`() {
        val result = DataChunkBuilder {
            contentKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `set contentKey results in control block with cpls 0`() {
        val result = DataChunkBuilder {
            contentKey = byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().cpls is_ Equal to_ 0)
    }

    @Test
    fun `having a contentKey makes the chunk version 1`() {
        val result = DataChunkBuilder {
            contentKey = byteArrayOf(1, 2, 3)
        }
        expect(result.version is_ Equal to_ 1)
    }

    @Test
    fun `added reference is accessible`() {
        DataChunkBuilder {
            references += byteArrayOf(1, 2, 3)
            expect(references is_ Equal to_ listOf(byteArrayOf(1, 2, 3)))
        }
    }

    @Test
    fun `add reference results in reference control block`() {
        val result = DataChunkBuilder {
            references += byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().type is_ Equal
                to_ DataChunkControlBlockType.ReferencedChunk)
    }

    @Test
    fun `add reference results in control block with reference as content`() {
        val result = DataChunkBuilder {
            references += byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().content is_ Equal to_ byteArrayOf(1, 2, 3))
    }

    @Test
    fun `add reference results in control block with cpls 0`() {
        val result = DataChunkBuilder {
            references += byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.single().cpls is_ Equal to_ 0)
    }

    @Test
    fun `order of control blocks is as expected`() {
        val result = DataChunkBuilder {
            references += byteArrayOf(1, 2, 3)
            contentKey = byteArrayOf(1, 2, 3)
            signature = byteArrayOf(1, 2, 3)
            publicKey = byteArrayOf(1, 2, 3)
            references += byteArrayOf(1, 2, 3)
        }
        expect(result.controlBlocks.map { it.type } is_ Equal to_ listOf(
                DataChunkControlBlockType.Signature,
                DataChunkControlBlockType.PublicKey,
                DataChunkControlBlockType.ContentKey,
                DataChunkControlBlockType.ReferencedChunk,
                DataChunkControlBlockType.ReferencedChunk))
    }

    @Test
    fun `signablePart contains everything after the signature`() {
        DataChunkBuilder {
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
                    0x00,                   // CEND
                    0x06))                  // payload
        }
    }

    @Test
    fun `createDataChunk creates chunk with given key`() =
            Given {
                createDataChunk(Key(Id("id"))) {}
            } when_ {
                key
            } then {
                expect(it.result is_ Equal to_ Key(Id("id")))
            }

    @Test
    fun `createDataChunk initializes chunk using DataChunkBuilder`() =
            Given {
                createDataChunk(Key(Id("id"))) {
                    payload = byteArrayOf(1, 2, 3)
                }
            } when_ {
                data
            } then {
                expect(it.result is_ Equal to_ byteArrayOf(0x00, 1, 2, 3))
            }
}
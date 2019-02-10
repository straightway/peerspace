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
import straightway.error.Panic
import straightway.expr.minus
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkControlBlockTest {

    private companion object {
        val content = byteArrayOf(0xA1.toByte(), 2, 3)
        val sut = DataChunkControlBlock(
                DataChunkControlBlockType.ReferencedChunk, 0xA, content)
        val binarySut = byteArrayOf(0x04, 0xA0.toByte(), 0x03) + content
    }

    private val test = Given { sut }

    @Test
    fun `type is accessible`() =
            test when_ {
                type
            } then {
                expect(it.result is_ Equal to_ DataChunkControlBlockType.ReferencedChunk)
            }

    @Test
    fun `cpls is accessible`() =
            test when_ {
                cpls
            } then {
                expect(it.result is_ Equal to_ 0xA)
            }

    @Test
    fun `content is accessible`() =
            test when_ {
                content
            } then {
                expect(it.result is_ Equal to_ content)
            }

    @Test
    fun `binary is as expected`() =
            test when_ {
                binary
            } then {
                expect(it.result is_ Equal to_ binarySut)
            }

    @Test
    fun `too big cpls panics`() =
            expect({
                DataChunkControlBlock(
                        DataChunkControlBlockType.ReferencedChunk, 0x10, byteArrayOf())
            }
                    does Throw.type<Panic>())

    @Test
    fun `negative cpls panics`() =
            expect({
                DataChunkControlBlock(
                        DataChunkControlBlockType.ReferencedChunk, -1, byteArrayOf())
            }
                    does Throw.type<Panic>())

    @Test
    fun `too big data panics`() =
            expect({
                DataChunkControlBlock(
                        DataChunkControlBlockType.ReferencedChunk, 0, ByteArray(4097))
            }
                    does Throw.type<Panic>())

    @Test
    fun `construction from binary`() {
        val sut = DataChunkControlBlock(binarySut)
        expect(sut.type is_ Equal to_ DataChunkControlBlockType.ReferencedChunk)
        expect(sut.cpls is_ Equal to_ 0xA)
        expect(sut.content is_ Equal to_ content)
    }

    @Test
    fun `construction from binary with offset`() {
        val sut = DataChunkControlBlock(ByteArray(1) + binarySut, 1)
        expect(sut.type is_ Equal to_ DataChunkControlBlockType.ReferencedChunk)
        expect(sut.cpls is_ Equal to_ 0xA)
        expect(sut.content is_ Equal to_ content)
    }

    @Test
    fun `toString yields proper string`() =
            test when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_
                        "DataChunkControlBlock(ReferencedChunk, 0xa, [a1 02 03])")
            }

    @Test
    fun `equals for two equal blocks is true`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()) is_ Equal
                to_ DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()))
    }

    @Test
    fun `two block with different type differ`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.PublicKey, 0x00, byteArrayOf()) is_ Not - Equal
                to_ DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()))
    }

    @Test
    fun `two block with different cpls differ`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x01, byteArrayOf()) is_ Not - Equal
                to_ DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()))
    }

    @Test
    fun `two block with different content differ`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()) is_ Not - Equal
                to_ DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf(1)))
    }

    @Test
    fun `a block is not equal to an instance of another type`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.PublicKey, 0x00, byteArrayOf()) is_ Not - Equal to_ 83)
    }

    @Test
    fun `hash codes for two equal blocks are Equal`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()).hashCode() is_
                Equal to_
                DataChunkControlBlock(
                        DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()).hashCode())
    }

    @Test
    fun `two block with different type have different hash codes`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.PublicKey, 0x00, byteArrayOf()).hashCode() is_
                Not - Equal to_
                DataChunkControlBlock(
                        DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()).hashCode())
    }

    @Test
    fun `two block with different cpls have different hash codes`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x01, byteArrayOf()).hashCode() is_
                Not - Equal to_
                DataChunkControlBlock(
                        DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()).hashCode())
    }

    @Test
    fun `two block with different content have different hash code`() {
        expect(DataChunkControlBlock(
                DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf()).hashCode() is_
                Not - Equal to_
                DataChunkControlBlock(
                        DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf(1)).hashCode())
    }

    @Test
    fun `binary size is equal to size of binary`() =
            Given {
                DataChunkControlBlock(
                        DataChunkControlBlockType.ContentKey, 0x00, byteArrayOf(1, 2, 3))
            } when_ {
                binarySize
            } then {
                expect(it.result is_ Equal to_ 6)
            }
}
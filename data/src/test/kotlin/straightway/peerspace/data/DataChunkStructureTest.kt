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
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.joinMultiLine

@Suppress("ReplaceCallWithBinaryOperator")
class DataChunkStructureTest {

    private companion object {
        const val VERSION2 = 2.toByte()
        val payload = byteArrayOf(4, 5, 6)
        val controlBlock = DataChunkControlBlock(
                DataChunkControlBlockType.ReferencedChunk,
                0xA,
                byteArrayOf(1, 2, 3))

        fun createArbitraryStructure() = DataChunkStructure.version2(
                listOf(
                        DataChunkControlBlock(
                                DataChunkControlBlockType.Signature,
                                DataChunkSignMode.EmbeddedKey.id,
                                byteArrayOf(1, 2, 3)),
                        DataChunkControlBlock(
                                DataChunkControlBlockType.PublicKey,
                                0x00,
                                byteArrayOf(4, 5, 6))),
                ByteArray(33) { it.toByte() })
    }

    private val test get() =
        Given {
            DataChunkStructure.version2(listOf(controlBlock), payload)
        }

    @Test
    fun `version is accessible`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ VERSION2)
            }

    @Test
    fun `controlBlocks is accessible`() =
            test when_ {
                controlBlocks
            } then {
                expect(it.result is_ Equal to_ listOf(controlBlock))
            }

    @Test
    fun `payload is accessible`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ payload)
            }

    @Test
    fun `created chunk has specified key`() =
            test when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.key is_ Equal to_ Key(Id("id")))
            }

    @Test
    fun `created chunk has specified data`() =
            test when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.data is_ Equal to_ binary)
            }

    @Test
    fun `data chunk from empty binary panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf()) } does Throw.type<Panic>())

    @Test
    fun `data chunk from binary with a future version panics`() =
            expect({
                DataChunkStructure.fromBinary(
                        byteArrayOf((DataChunkStructure.Header.MAX_SUPPORTED_VERSION + 1).toByte()))
            } does Throw.type<Panic>())

    @Test
    fun `toString yields proper result`() =
            Given {
                createArbitraryStructure()
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "DataChunkStructure " +
                        listOf(
                                "version: $VERSION2",
                                "control blocks ${this.controlBlocks.joinMultiLine(2)}\n" +
                                        "payload (size: 33):\n" +
                                        "  00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
                                        "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f\n" +
                                        "  20").joinMultiLine(2))
            }

    @Test
    fun `two equal chunks equal`() =
            Given {
                object {
                    val a = createArbitraryStructure()
                    val b = createArbitraryStructure()
                }
            } when_ {
                a.equals(b)
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `two different chunks differ`() =
            Given {
                object {
                    val a = DataChunkStructure.version2(listOf(), byteArrayOf())
                    val b = createArbitraryStructure()
                }
            } when_ {
                a.equals(b)
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `DataChunkStructure is not equal to other class instances`() =
            Given {
                object {
                    val a = DataChunkStructure.version2(listOf(), byteArrayOf())
                    val b = 83
                }
            } when_ {
                a.equals(b)
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `two equal blocks have equal hash codes`() =
            Given {
                object {
                    val a = createArbitraryStructure()
                    val b = createArbitraryStructure()
                }
            } when_ {
                a.hashCode() == b.hashCode()
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `two different blocks have different hash codes`() =
            Given {
                object {
                    val a = DataChunkStructure.version2(listOf(), byteArrayOf())
                    val b = createArbitraryStructure()
                }
            } when_ {
                a.hashCode() == b.hashCode()
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `MAX_SUPPORTED_VERSION is 2`() =
            expect(DataChunkStructure.Header.MAX_SUPPORTED_VERSION is_ Equal to_ 2)

    @Test
    fun `VERSION_FIELD_SIZE is 1`() =
            expect(DataChunkStructure.Header.VERSION_FIELD_SIZE is_ Equal to_ 1)
}
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
import straightway.error.Panic
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.impl.DataChunkStructure.Companion.version
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkStructureTest {

    @Test
    fun `MAX_SUPPORTED_VERSION is 3`() =
            expect(DataChunkStructure.MAX_SUPPORTED_VERSION is_ Equal to_ 3)

    @Test
    fun `VERSION_FIELD_SIZE is one byte`() =
            expect(DataChunkStructure.VERSION_FIELD_SIZE is_ Equal to_ Byte.SIZE_BYTES)

    @Test
    fun `fromBinary with version 0 yields version 0 chunk`() =
            Given {
                DataChunkVersion0(byteArrayOf(1, 2, 3))
            } when_ {
                DataChunkStructure.fromBinary(binary)
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `fromBinary with version 1 yields version 1 chunk`() =
            Given {
                DataChunkVersion1(byteArrayOf(1, 2, 3), 5)
            } when_ {
                DataChunkStructure.fromBinary(binary)
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `fromBinary with version 2 yields version 2 chunk`() =
            Given {
                DataChunkVersion2(
                        listOf(DataChunkControlBlock(
                                DataChunkControlBlockType.ReferencedChunk,
                                0x0,
                                byteArrayOf(4, 5, 6))),
                        byteArrayOf(1, 2, 3))
            } when_ {
                DataChunkStructure.fromBinary(binary)
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `fromBinary with version 3 yields version 3 chunk`() =
            Given {
                DataChunkVersion3(byteArrayOf(4, 5, 6), byteArrayOf(1, 2, 3))
            } when_ {
                DataChunkStructure.fromBinary(binary)
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `fromBinary with invalid version panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf(-1)) } does Throw.type<Panic>())

    @Test
    fun `fromBinary with empty data panics`() =
            expect({ DataChunkStructure.fromBinary(byteArrayOf()) } does Throw.type<Panic>())

    @Test
    fun `fromBinary with a future version panics`() =
            expect({
                DataChunkStructure.fromBinary(
                        byteArrayOf((DataChunkStructure.MAX_SUPPORTED_VERSION + 1).toByte()))
            } does Throw.type<Panic>())

    @Test
    fun `version yields first byte from array`() =
            expect(byteArrayOf(1).version is_ Equal to_ 1.toByte())

    @Test
    fun `version for empty array panics`() =
            expect({ byteArrayOf().version } does Throw.type<Panic>())

    @Test
    fun `created chunk has specified key`() =
            Given {
                DataChunkVersion0(byteArrayOf(1, 2, 3)) as DataChunkStructure
            } when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.key is_ Equal to_ Key(Id("id")))
            }

    @Test
    fun `created chunk has specified data`() =
            Given {
                DataChunkVersion0(byteArrayOf(1, 2, 3)) as DataChunkStructure
            } when_ {
                createChunk(Key(Id("id")))
            } then {
                expect(it.result.data is_ Equal to_ binary)
            }
}
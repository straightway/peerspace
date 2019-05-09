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
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.indent
import straightway.utils.toByteArray
import straightway.utils.toHexBlocks

class DataChunkVersion3Test : DataChunkStructureBaseTest<DataChunkVersion3>() {

    private companion object {
        val contentKey = byteArrayOf(1, 2, 3)
        val expectedBinary =
                byteArrayOf(0x03) + // Version
                contentKey.size.toByteArray(Short.SIZE_BYTES) +
                contentKey +
                testPayload
    }

    override fun createSut(payload: ByteArray) =
            DataChunkVersion3(contentKey, payload)

    @Test
    fun `has proper version number`() =
            test when_ {
                version
            } then {
                expect(it.result is_ Equal to_ 3)
            }

    @Test
    fun `binary yields proper binary representation`() =
            test when_ {
                binary
            } then {
                expect(it.result is_ Equal to_ expectedBinary)
            }

    @Test
    fun `payload accessible if constructed with binary content key and payload`() =
            test when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `content key accessible if constructed with binary content key and payload`() =
            test when_ {
                contentKey
            } then {
                expect(it.result is_ Equal to_ contentKey)
            }

    @Test
    fun `construction with content key size which does not fit into 16 bits panics`() =
            expect({ DataChunkVersion3(ByteArray(0x10000), testPayload) } does Throw.type<Panic>())

    @Test
    fun `construction from binary has proper payload`() =
            Given {
                DataChunkVersion3.fromBinary(expectedBinary)
            } when_ {
                payload
            } then {
                expect(it.result is_ Equal to_ testPayload)
            }

    @Test
    fun `construction from binary has proper content key`() =
            Given {
                DataChunkVersion3.fromBinary(expectedBinary)
            } when_ {
                contentKey
            } then {
                expect(it.result is_ Equal to_ contentKey)
            }

    @Test
    fun `construction from binary with invalid key size throws`() =
            expect({ DataChunkVersion3.fromBinary(byteArrayOf(0x03, 0x1F, 0x1F)) }
                    does Throw.exception)

    @Test
    fun `construction from binary with invalid version panics`() =
            expect({ DataChunkVersion3.fromBinary(byteArrayOf(0x00) + expectedBinary) }
                    does Throw.type<Panic>())

    @Test
    fun `toString yields proper result`() =
            Given {
                DataChunkVersion3(
                        ByteArray(33) { it.toByte() },
                        ByteArray(34) { (64 + it).toByte() })
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "DataChunkVersion3 {\n" +
                        "  content key (size: ${contentKey.size}):\n" +
                        contentKey.toHexBlocks(32).indent(4) + "\n" +
                        "  payload (size: ${payload.size}):\n" +
                        payload.toHexBlocks(32).indent(4) + "\n" +
                        "}")
            }
}
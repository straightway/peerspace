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
import straightway.testing.bdd.Given
import straightway.testing.flow.False
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_

@Suppress("ReplaceCallWithBinaryOperator")
abstract class DataChunkStructureBaseTest<T: DataChunkStructure> {

    protected companion object {
        val testPayload = byteArrayOf(4, 5, 6)
    }

    protected val test get() =
        Given { createSut(testPayload) }

    protected abstract fun createSut(payload: ByteArray): T

    @Test
    fun `two equal chunks equal`() =
            Given {
                object {
                    val a = createSut(testPayload)
                    val b = createSut(testPayload)
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
                    val a = createSut(testPayload)
                    val b = createSut(testPayload + 0x01)
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
                    val a = createSut(testPayload)
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
                    val a = createSut(testPayload)
                    val b = createSut(testPayload)
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
                    val a = createSut(testPayload)
                    val b = createSut(testPayload + 0x01)
                }
            } when_ {
                a.hashCode() == b.hashCode()
            } then {
                expect(it.result is_ False)
            }
}
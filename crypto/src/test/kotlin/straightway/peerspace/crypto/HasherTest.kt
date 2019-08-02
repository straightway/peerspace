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
package straightway.peerspace.crypto

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.testing.bdd.Given
import straightway.testing.flow.*
import straightway.utils.serializeToByteArray
import straightway.utils.toByteArray
import java.io.Serializable

class HasherTest {

    @Test
    fun `hash value of serializable object is computed from raw binary representation`() =
            Given {
                mock<Hasher>()
            } when_ {
                getHash(TestSerializable(83))
            } then {
                verify(this).getHash(eq(TestSerializable(83).serializeToByteArray()))
            }

    @Test
    fun `hash value of string is computed from raw string bytes in UTF8`() =
            Given {
                mock<Hasher>()
            } when_ {
                getHash("string")
            } then {
                verify(this).getHash(eq("string".toByteArray()))
            }

    @Test
    fun `hash value of string as Serializable is computed from raw string bytes in UTF8`() =
            Given {
                mock<Hasher>()
            } when_ {
                getHash("string" as Serializable)
            } then {
                verify(this).getHash(eq("string".toByteArray()))
            }

    @Test
    fun `hashBytes for hasher with 0 hashBits panics`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(0) }
            } when_ {
                hashBytes
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `hashBytes for hasher with 1 hashBits is 1`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(1) }
            } when_ {
                hashBytes
            } then {
                expect(it.result is_ Equal to_ 1)
            }

    @Test
    fun `hashBytes for hasher with 9 hashBits is 2`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(9) }
            } when_ {
                hashBytes
            } then {
                expect(it.result is_ Equal to_ 2)
            }

    @Test
    fun `hashBytes for hasher with 8 hashBits is 1`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(8) }
            } when_ {
                hashBytes
            } then {
                expect(it.result is_ Equal to_ 1)
            }

    @Test
    fun `hashBytes for hasher with 16 hashBits is 2`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(16) }
            } when_ {
                hashBytes
            } then {
                expect(it.result is_ Equal to_ 2)
            }

    @Test
    fun `hashBytes for hasher with 17 hashBits is 3`() =
            Given {
                mock<Hasher> { on { hashBits }.thenReturn(17) }
            } when_ {
                hashBytes
            } then {
                expect(it.result is_ Equal to_ 3)
            }

    private data class TestSerializable(val i: Int) : Serializable {
        companion object { const val serialVersionUID = 1L }
    }
}
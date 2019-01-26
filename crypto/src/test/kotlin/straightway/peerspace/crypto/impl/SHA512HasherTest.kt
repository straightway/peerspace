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
package straightway.peerspace.crypto.impl

import org.junit.jupiter.api.Test
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.security.MessageDigest

class SHA512HasherTest {

    private companion object {
        val valueToHash = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        val expectedHash = MessageDigest.getInstance("SHA-512").digest(valueToHash)
    }

    @Test
    fun `hasher uses SHA-512`() =
            Given {
                SHA512Hasher()
            } when_ {
                getHash(valueToHash)
            } then {
                expect(it.result is_ Equal to_ expectedHash)
            }

    @Test
    fun `hashBits is according to bits of hashed value`() =
            Given {
                SHA512Hasher()
            } when_ {
                getHash(valueToHash)
            } then {
                expect(it.result.size * Byte.SIZE_BITS is_ Equal to_ hashBits)
            }

    @Test
    fun `algorithmId is HSA512`() =
            Given {
                SHA512Hasher()
            } when_ {
                algorithm
            } then {
                expect(it.result is_ Equal to_ "SHA512")
            }
}
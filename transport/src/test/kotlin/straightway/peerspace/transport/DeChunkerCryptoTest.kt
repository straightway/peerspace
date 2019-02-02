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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.crypto.Decryptor
import straightway.peerspace.crypto.SignatureChecker
import straightway.testing.bdd.Given
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.testAutoGeneratedDataClassMethods

class DeChunkerCryptoTest {

    @Test
    fun testAutoGeneratedDataClassMethods() =
            DeChunkerCrypto(mock(), mock()).testAutoGeneratedDataClassMethods()

    @Test
    fun `signatureChecker is accessible`() {
        val signatureCheckerIn: SignatureChecker = mock()
        Given {
            DeChunkerCrypto(signatureChecker = signatureCheckerIn)
        } when_ {
            signatureChecker
        } then {
            expect(it.result is_ Same as_ signatureCheckerIn)
        }
    }

    @Test
    fun `decryptor is accessible`() {
        val decryptorIn: Decryptor = mock()
        Given {
            DeChunkerCrypto(decryptor = decryptorIn)
        } when_ {
            decryptor
        } then {
            expect(it.result is_ Same as_ decryptorIn)
        }
    }
}
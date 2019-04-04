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

import org.junit.jupiter.api.Test
import straightway.peerspace.crypto.impl.AES256Cryptor
import straightway.peerspace.crypto.impl.RSA2048Cryptor
import straightway.peerspace.crypto.impl.RSA2048SignatureChecker
import straightway.peerspace.crypto.impl.RSA2048Signer
import straightway.peerspace.crypto.impl.RSA2048TestEnvironment
import straightway.peerspace.crypto.impl.SHA512Hasher
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray

class CryptoFactoryTest {

    @Test
    fun `default factory yields AES256Cryptor as symmetricCryptor`() =
            expect(CryptoFactory().createSymmetricCryptor() is AES256Cryptor)

    @Test
    fun `default factory yields AES256Cryptor as symmetricCryptor from binary`() =
            expect(CryptoFactory().getSymmetricCryptor(ByteArray(16)) is AES256Cryptor)

    @Test
    fun `default factory yields RSA2048Cryptor as cryptoIdentity`() =
            expect(CryptoFactory().createCryptoIdentity() is RSA2048Cryptor)

    @Test
    fun `default factory yields RSA2048SignatureChecker as signatureChecker from binary`() =
            expect(CryptoFactory().getSignatureChecker(RSA2048TestEnvironment
                    .cryptors[0].signatureCheckKey) is RSA2048SignatureChecker)

    @Test
    fun `default factory yields RSA2048Signer as signer from binary`() =
            expect(CryptoFactory().getSigner(RSA2048TestEnvironment
                    .cryptors[0].signKey) is RSA2048Signer)

    @Test
    fun `default factory yields SHA512Hasher as hasher`() =
            expect(CryptoFactory().createHasher() is SHA512Hasher)

    @Test
    fun `default factory yields SHA512 as hash algorithm`() =
            expect(CryptoFactory().hashAlgorithm is_ Equal to_ "SHA512")

    @Test
    fun `default factory is serializable`() =
            Given {
                CryptoFactory()
            } when_ {
                serializeToByteArray()
            } then {
                val deserialized = it.result.deserializeTo<CryptoFactory>()
                expect(deserialized.hashAlgorithm is_ Equal to_ "SHA512")
            }
}
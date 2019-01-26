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
import straightway.peerspace.crypto.SignatureChecker
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray

class RSA2048SignatureCheckerTest {

    companion object {
        val sut = with(RSA2048TestEnvironment) {
            RSA2048SignatureChecker(cryptors[0].keyPair.public, factory)
        }
        val matchingCryptor = RSA2048TestEnvironment.cryptors[0]
        val notMatchingCryptor = RSA2048TestEnvironment.cryptors[1]
    }

    @Test
    fun `can be created using raw key bytes`() =
            Given {
                RSA2048SignatureChecker(
                        matchingCryptor.keyPair.public.encoded, RSA2048TestEnvironment.factory)
            } when_ {
                matchingCryptor.sign(byteArrayOf(1, 2, 3))
            } then {
                expect(isSignatureValid(byteArrayOf(1, 2, 3), it.result) is_ True)
            }

    @Test
    fun `encrypted can be decrypted by matching cryptor`() =
            Given {
                sut
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect(matchingCryptor.decrypt(it.result) is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `encrypted can not be decrypted by not matching cryptor`() =
            Given {
                sut
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect({ notMatchingCryptor.decrypt(it.result) } does Throw.exception)
            }

    @Test
    fun `signature of matching cryptor can be verified`() =
            Given {
                sut
            } when_ {
                matchingCryptor.sign(byteArrayOf(1, 2, 3))
            } then {
                expect(isSignatureValid(byteArrayOf(1, 2, 3), it.result) is_ True)
            }

    @Test
    fun `signature from not matching cryptor can not be verified`() =
            Given {
                sut
            } when_ {
                notMatchingCryptor.sign(byteArrayOf(1, 2, 3))
            } then {
                expect(isSignatureValid(byteArrayOf(1, 2, 3), it.result) is_ False)
            }

    @Test
    fun `encryptionKey is public key`() =
            Given {
                sut
            } when_ {
                encryptionKey
            } then {
                expect(it.result is_ Equal to_ matchingCryptor.keyPair.public.encoded)
            }

    @Test
    fun `signatureCheckKey is public key`() =
            Given {
                sut
            } when_ {
                signatureCheckKey
            } then {
                expect(it.result is_ Equal to_ matchingCryptor.keyPair.public.encoded)
            }

    @Test
    fun `keyBits is 2048`() =
            Given {
                sut
            } when_ {
                keyBits
            } then {
                expect(it.result is_ Equal to_ 2048)
            }

    @Test
    fun `cryptor is serializable`() =
            Given {
                sut
            } when_ {
                serializeToByteArray()
            } then {
                val deserialzed = it.result.deserializeTo<SignatureChecker>()
                val signature = matchingCryptor.sign(byteArrayOf(1, 2, 3))
                expect(deserialzed.isSignatureValid(byteArrayOf(1, 2, 3), signature) is_ True)
            }

    @Test
    fun `hashAlgorithm is SHA512`() =
            Given {
                RSA2048SignerTest.sut
            } when_ {
                hashAlgorithm
            } then {
                expect(it.result is_ Equal to_ "SHA512")
            }

    @Test
    fun `has serialVersionUID`() =
            expect(RSA2048Cryptor.serialVersionUID is_ Equal to_ 1L)
}
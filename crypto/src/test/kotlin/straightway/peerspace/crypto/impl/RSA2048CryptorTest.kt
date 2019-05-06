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
import straightway.expr.minus
import straightway.peerspace.crypto.CryptoIdentity
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Size
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.of
import straightway.testing.flow.to_
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray

class RSA2048CryptorTest {

    companion object {
        val sut = RSA2048TestEnvironment.cryptors[0]
    }

    @Test
    fun `new instance generates a new key`() =
            Given {
                sut
            } when_ {
                keyPair
            } then {
                expect(it.result.public.encoded is_ Not - Empty)
                expect(it.result.private.encoded is_ Not - Empty)
            }

    @Test
    fun `two instance generate different keys`() {
        expect(sut.keyPair.public.encoded is_ Not - Equal to_
                RSA2048TestEnvironment.cryptors[1].keyPair.public.encoded)
        expect(sut.keyPair.private.encoded is_ Not - Equal to_
                RSA2048TestEnvironment.cryptors[1].keyPair.private.encoded)
    }

    @Test
    fun `encrypted can be decrypted by same cryptor`() =
            Given {
                sut
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect(decrypt(it.result) is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `encrypted can not be decrypted by other cryptor`() =
            Given {
                sut
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect({ RSA2048TestEnvironment.cryptors[1].decrypt(it.result) }
                        does Throw.exception)
            }

    @Test
    fun `signature can be verified with the same cryptor`() =
            Given {
                sut
            } when_ {
                sign(byteArrayOf(1, 2, 3))
            } then {
                expect(isSignatureValid(byteArrayOf(1, 2, 3), it.result) is_ True)
            }

    @Test
    fun `signature can not be verified with the other cryptor`() =
            Given {
                sut
            } when_ {
                sign(byteArrayOf(1, 2, 3))
            } then {
                with(RSA2048TestEnvironment) {
                    expect(cryptors[1].isSignatureValid(byteArrayOf(1, 2, 3), it.result) is_ False)
                }
            }

    @Test
    fun `signKey is private key`() =
            Given {
                sut
            } when_ {
                signKey
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(CipherAlgorithm.RSA2048.encoded) + keyPair.private.encoded)
            }

    @Test
    fun `decryptionKey is private key`() =
            Given {
                sut
            } when_ {
                decryptionKey
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(CipherAlgorithm.RSA2048.encoded) + keyPair.private.encoded)
            }

    @Test
    fun `encryptionKey is public key`() =
            Given {
                sut
            } when_ {
                encryptionKey
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(CipherAlgorithm.RSA2048.encoded) + keyPair.public.encoded)
            }

    @Test
    fun `signatureCheckKey is public key`() =
            Given {
                sut
            } when_ {
                signatureCheckKey
            } then {
                expect(it.result is_ Equal to_
                        byteArrayOf(CipherAlgorithm.RSA2048.encoded) + keyPair.public.encoded)
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
    fun `hashAlgorithm is SHA512`() =
            Given {
                RSA2048SignerTest.sut
            } when_ {
                hashAlgorithm
            } then {
                expect(it.result is_ Equal to_ "SHA512")
            }

    @Test
    fun `fixedCipherTextBytes is equal to the size of the cipher text`() =
            (1..sut.encryptorProperties.maxClearTextBytes).forEach {
                expect(sut.encrypt(ByteArray(it)) has
                        Size of sut.decryptorProperties.fixedCipherTextBytes)
            }

    @Test
    fun `cryptor is serializable`() =
            Given {
                sut
            } when_ {
                serializeToByteArray()
            } then {
                val deserialzed = it.result.deserializeTo<CryptoIdentity>()
                val signature = sign(byteArrayOf(1, 2, 3))
                expect(deserialzed.isSignatureValid(byteArrayOf(1, 2, 3), signature) is_ True)
            }

    @Test
    fun `algorithm is RSA2048`() =
            Given {
                sut
            } when_ {
                algorithm
            } then {
                expect(it.result is_ Equal to_ "RSA2048")
            }

    @Test
    fun `has serialVersionUID`() =
            expect(RSA2048Cryptor.serialVersionUID is_ Equal to_ 1L)
}
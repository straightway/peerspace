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
import straightway.peerspace.crypto.Cryptor
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray

class AES256CryptorTest {

    @Test
    fun `new instance generates a new key`() =
            Given {
                AES256Cryptor()
            } when_ {
                key
            } then {
                expect(it.result.encoded is_ Not - Empty)
            }

    @Test
    fun `two instance generates have different keys`() =
            Given {
                AES256Cryptor()
            } when_ {
                key
            } then {
                expect(it.result.encoded is_ Not - Equal to_ AES256Cryptor().key)
            }

    @Test
    fun `encrypted can be decrypted by same cryptor`() =
            Given {
                AES256Cryptor()
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect(decrypt(it.result) is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `encrypted can not be decrypted by other cryptor`() =
            Given {
                AES256Cryptor()
            } when_ {
                encrypt(byteArrayOf(1, 2, 3))
            } then {
                expect({ AES256Cryptor().decrypt(it.result) } does Throw.exception)
            }

    @Test
    fun `two cryptor instances with the same key passed can decrypt encrypted text`() {
        val keyBytes = ByteArray(AES256Cryptor.keyBits / Byte.SIZE_BITS) { it.toByte() }
        Given {
            AES256Cryptor(keyBytes)
        } when_ {
            encrypt(byteArrayOf(1, 2, 3))
        } then {
            expect(AES256Cryptor(keyBytes).decrypt(it.result)
                    is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `encryption key is the key bytes`() {
        val keyBytes = ByteArray(AES256Cryptor.keyBits / Byte.SIZE_BITS) { it.toByte() }
        Given {
            AES256Cryptor(keyBytes)
        } when_ {
            encryptionKey
        } then {
            expect(it.result is_ Equal to_ keyBytes)
        }
    }

    @Test
    fun `decryption key is the key bytes`() {
        val keyBytes = ByteArray(AES256Cryptor.keyBits / Byte.SIZE_BITS) { it.toByte() }
        Given {
            AES256Cryptor(keyBytes)
        } when_ {
            encryptionKey
        } then {
            expect(it.result is_ Equal to_ keyBytes)
        }
    }

    @Test
    fun `cryptor is serializable`() {
        lateinit var encrypted: ByteArray
        Given {
            AES256Cryptor()
        } while_ {
            encrypted = encrypt(byteArrayOf(1, 2, 3))
        } when_ {
            serializeToByteArray()
        } then {
            val deserializedCryptor = it.result.deserializeTo<Cryptor>()
            expect(deserializedCryptor.decrypt(encrypted) is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `keyBits is 256`() =
            Given {
                AES256Cryptor()
            } when_ {
                keyBits
            } then {
                expect(it.result is_ Equal to_ 256)
            }

    @Test
    fun `block size is 16`() =
            Given {
                AES256Cryptor()
            } when_ {
                encryptorProperties.blockBytes
            } then {
                expect(it.result is_ Equal to_ 16)
            }

    @Test
    fun `max clear text size is unconstrained`() =
            Given {
                AES256Cryptor()
            } when_ {
                encryptorProperties.maxClearTextBytes
            } then {
                expect(it.result is_ Equal to_ Int.MAX_VALUE)
            }

    @Test
    fun `output size for input size`() =
            Given {
                AES256Cryptor()
            } when_ {
                encryptorProperties.getOutputBytes(2 * encryptorProperties.blockBytes)
            } then {
                expect(it.result is_ Equal to_ 3 * encryptorProperties.blockBytes)
            }

    @Test
    fun `fixedCipherTextSize is zero, because there is no fixed size`() =
            Given {
                AES256Cryptor()
            } when_ {
                decryptorProperties.fixedCipherTextBytes
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `has serialVersionUID`() =
            expect(AES256Cryptor.serialVersionUID is_ Equal to_ 1L)
}
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

import straightway.peerspace.crypto.impl.AES256Cryptor
import straightway.peerspace.crypto.impl.RSA2048Cryptor
import straightway.peerspace.crypto.impl.RSA2048SignatureChecker
import straightway.peerspace.crypto.impl.RSA2048Signer
import straightway.peerspace.crypto.impl.SHA512Hasher
import java.io.Serializable

/**
 * Factory for all kinds of cryptographic objects.
 */
@Suppress("ComplexInterface", "TooManyFunctions")
interface CryptoFactory {
    fun createSymmetricCryptor(): Cryptor
    fun createCryptoIdentity(): CryptoIdentity
    fun createHasher(): Hasher
    fun getSymmetricCryptor(rawKey: ByteArray): Cryptor
    fun getSignatureChecker(rawKey: ByteArray): SignatureChecker
    fun getSigner(rawKey: ByteArray): Signer
    val hashAlgorithm: String

    companion object {
        operator fun invoke() = DefaultFactory() as CryptoFactory
        private class DefaultFactory : CryptoFactory, Serializable {
            override fun createSymmetricCryptor() =
                    AES256Cryptor()
            override fun createCryptoIdentity() =
                    RSA2048Cryptor(this)
            override fun createHasher() =
                    SHA512Hasher()
            override fun getSymmetricCryptor(rawKey: ByteArray) =
                    AES256Cryptor(rawKey)
            override fun getSignatureChecker(rawKey: ByteArray) =
                    RSA2048SignatureChecker(rawKey, this)
            override fun getSigner(rawKey: ByteArray) =
                    RSA2048Signer(rawKey, this)
            override val hashAlgorithm by lazy { createHasher().algorithm }
            companion object { const val serialVersionUID = 1L }
        }
    }
}
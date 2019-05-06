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

import straightway.peerspace.crypto.CryptoFactory
import straightway.peerspace.crypto.DecryptorProperties
import straightway.peerspace.crypto.Signer
import java.io.Serializable
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher

/**
 * Signer implementation generating signatures and decrypting payload keys
 * using RSA2048, while the payload itself is symmetrically decrypted.
 */
class RSA2048Signer(
        val key: PrivateKey,
        factory: CryptoFactory
) : Signer, DecryptorProperties, Serializable {

    constructor(rawKey: ByteArray, factory: CryptoFactory) :
            this(privateKeyFrom(
                    rawKey.stripAndCheckAlgorithmType(CipherAlgorithm.RSA2048),
                    RSA2048Cryptor.keyCipherAlgorithm),
                factory)

    override val keyBits = RSA2048Cryptor.keyBits
    override val algorithm = "RSA$keyBits"

    override val decryptorProperties get() = this

    override val fixedCipherTextBytes = (keyBits - 1) / Byte.SIZE_BITS + 1

    override val signKey get() = key.encoded!!.with(CipherAlgorithm.RSA2048)
    override val decryptionKey get() = key.encoded!!.with(CipherAlgorithm.RSA2048)

    override val hashAlgorithm: String = factory.hashAlgorithm

    override fun sign(toSign: ByteArray) =
            with(rawSigner) { update(toSign); sign()!! }

    override fun decrypt(toDecrypt: ByteArray) =
            decryptCipher.doFinal(toDecrypt)!!

    private val rawSigner get() =
            Signature.getInstance(signAlgorithm)!!.apply { initSign(key) }

    private val signAlgorithm =
            "${hashAlgorithm}with${RSA2048Cryptor.keyCipherAlgorithm}"

    private val decryptCipher: Cipher get() =
            Cipher.getInstance(RSA2048Cryptor.keyCipherAlgorithm)!!.apply {
                init(Cipher.DECRYPT_MODE, key)
            }

    companion object {
        const val serialVersionUID = 1L
    }
}
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
import straightway.peerspace.crypto.EncryptorProperties
import straightway.peerspace.crypto.SignatureChecker
import java.io.Serializable
import java.security.PublicKey
import java.security.Signature
import javax.crypto.Cipher

/**
 * Signature checker implementation checking signatures and decrypting payload keys
 * using RSA2048, while the payload itself is symmetrically decrypted.
 */
class RSA2048SignatureChecker(
        val key: PublicKey,
        factory: CryptoFactory
) : SignatureChecker, EncryptorProperties, Serializable {

    constructor(rawKey: ByteArray, factory: CryptoFactory) :
            this(publicKeyFrom(
                    rawKey.stripAndCheckAlgorithmType(CipherAlgorithm.RSA2048),
                    RSA2048Cryptor.keyCipherAlgorithm),
                factory)

    override val algorithm = "RSA2048"
    override val keyBits = RSA2048Cryptor.keyBits

    override val encryptorProperties get() = this

    @Suppress("MagicNumber")
    override val maxClearTextBytes = 245
    override val blockBytes = 0
    override fun getOutputBytes(inputSize: Int) = encryptCipher.getOutputSize(inputSize)

    override val signatureCheckKey get() = key.encoded!!.with(CipherAlgorithm.RSA2048)
    override val encryptionKey get() = key.encoded!!.with(CipherAlgorithm.RSA2048)

    override val hashAlgorithm = factory.hashAlgorithm

    override fun isSignatureValid(signed: ByteArray, signature: ByteArray) =
            with(signer) { update(signed); verify(signature) }

    override fun encrypt(toEncrypt: ByteArray) =
            encryptCipher.doFinal(toEncrypt)!!

    companion object {
        const val serialVersionUID = 1L
    }

    private val signer get() =
        Signature.getInstance(signAlgorithm)!!.apply { initVerify(key) }

    private val signAlgorithm =
            "${hashAlgorithm}with${RSA2048Cryptor.keyCipherAlgorithm}"

    private val encryptCipher: Cipher get() =
            Cipher.getInstance(RSA2048Cryptor.keyCipherAlgorithm).apply {
                init(Cipher.ENCRYPT_MODE, key)
            }
}
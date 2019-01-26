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
import straightway.peerspace.crypto.Cryptor
import straightway.peerspace.crypto.SignatureChecker
import straightway.utils.toByteArray
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
        private val factory: CryptoFactory
) : SignatureChecker, Serializable {

    constructor(rawKey: ByteArray, factory: CryptoFactory) :
            this(publicKeyFrom(rawKey, RSA2048Cryptor.keyCipherAlgorithm), factory)

    override val signatureCheckKey get() = key.encoded!!
    override val encryptionKey get() = key.encoded!!

    override val keyBits = RSA2048Cryptor.keyBits

    override val hashAlgorithm = factory.hashAlgorithm

    override fun isSignatureValid(signed: ByteArray, signature: ByteArray) =
            with(signer) { update(signed); verify(signature) }

    override fun encrypt(toEncrypt: ByteArray) =
            with(factory.createSymmetricCryptor()) { encryptedPayloadKey + encrypt(toEncrypt) }

    companion object {
        const val serialVersionUID = 1L
    }

    private val signer get() =
        Signature.getInstance(signAlgorithm)!!.apply { initVerify(key) }

    private val signAlgorithm =
            "${hashAlgorithm}with${RSA2048Cryptor.keyCipherAlgorithm}"

    private val Cryptor.encryptedPayloadKey: ByteArray
        get() {
            val encryptedPayloadKey = encryptCipher.doFinal(decryptionKey)!!
            val encryptedPayloadKeyLength = encryptedPayloadKey.size.toByteArray()
            return encryptedPayloadKeyLength + encryptedPayloadKey
        }

    private val encryptCipher: Cipher get() =
            Cipher.getInstance(RSA2048Cryptor.keyCipherAlgorithm).apply {
                init(Cipher.ENCRYPT_MODE, key)
            }
}
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
import straightway.peerspace.crypto.Signer
import straightway.utils.getInt
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
        private val factory: CryptoFactory
) : Signer, Serializable {

    constructor(rawKey: ByteArray, factory: CryptoFactory) :
            this(privateKeyFrom(rawKey, RSA2048Cryptor.keyCipherAlgorithm), factory)

    override val signKey get() = key.encoded!!
    override val decryptionKey get() = key.encoded!!

    override val keyBits = RSA2048Cryptor.keyBits

    override val hashAlgorithm: String = factory.hashAlgorithm

    override fun sign(toSign: ByteArray) =
            with(rawSigner) { update(toSign); sign()!! }

    override fun decrypt(toDecrypt: ByteArray) =
            with(toDecrypt) { factory.getSymmetricCryptor(payloadKey).decrypt(payload) }

    private val rawSigner get() =
            Signature.getInstance(signAlgorithm)!!.apply { initSign(key) }

    private val signAlgorithm =
            "${hashAlgorithm}with${RSA2048Cryptor.keyCipherAlgorithm}"

    private val ByteArray.payload get() =
            slice(payloadArea).toByteArray()

    private val ByteArray.payloadArea get() =
            payloadKeyArea.endInclusive + 1 until size

    private val ByteArray.payloadKey get() =
            decryptCipher.doFinal(encryptedPayloadKey)

    private val ByteArray.encryptedPayloadKey get() =
            slice(payloadKeyArea).toByteArray()

    private val ByteArray.payloadKeyArea get() =
            payloadKeyLengthArea.followedBy(payloadKeyLength)

    private val ByteArray.payloadKeyLength get() =
            slice(payloadKeyLengthArea).toByteArray().getInt()

    private val decryptCipher: Cipher get() =
            Cipher.getInstance(RSA2048Cryptor.keyCipherAlgorithm)!!.apply {
                init(Cipher.DECRYPT_MODE, key)
            }

    private fun IntRange.followedBy(numBytes: Int) =
            endInclusive + 1..endInclusive + numBytes

    companion object {
        const val serialVersionUID = 1L
        private val payloadKeyLengthArea = 0 until Int.SIZE_BYTES
    }
}
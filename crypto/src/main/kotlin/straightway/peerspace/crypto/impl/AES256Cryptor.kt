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

import straightway.peerspace.crypto.Cryptor
import straightway.peerspace.crypto.DecryptorProperties
import straightway.peerspace.crypto.EncryptorProperties
import java.io.Serializable
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptor implementation for AES256.
 */
class AES256Cryptor private constructor (
        val key: SecretKey
) : Cryptor, EncryptorProperties, DecryptorProperties, Serializable {

    constructor() : this(with(KeyGenerator.getInstance("AES")) {
        init(AES256Cryptor.keyBits); generateKey()
    })

    constructor(keyBytes: ByteArray) :
            this(SecretKeySpec(keyBytes.stripAndCheckAlgorithmType(CipherAlgorithm.AES256), "AES"))

    override val keyBits = AES256Cryptor.keyBits
    override val algorithm = "AES$keyBits"

    override val encryptorProperties get() = this
    override val decryptorProperties get() = this

    override val maxClearTextBytes = Int.MAX_VALUE
    override val blockBytes by lazy { encryptCipher.blockSize }
    override val fixedCipherTextBytes = 0
    override fun getOutputBytes(inputSize: Int) = encryptCipher.getOutputSize(inputSize)

    override val encryptionKey get() = key.encoded.with(CipherAlgorithm.AES256)
    override val decryptionKey get() = encryptionKey

    override fun encrypt(toEncrypt: ByteArray) = encryptCipher.doFinal(toEncrypt)
    override fun decrypt(toDecrypt: ByteArray) = decryptCipher.doFinal(toDecrypt)

    companion object {
        const val keyBits = 256
        const val serialVersionUID = 1L
        const val algorithm = "AES/ECB/PKCS5Padding"
    }

    private val decryptCipher: Cipher get() = getCipher(Cipher.DECRYPT_MODE)
    private val encryptCipher: Cipher get() = getCipher(Cipher.ENCRYPT_MODE)
    private fun getCipher(mode: Int) =
            Cipher.getInstance(Companion.algorithm).apply { init(mode, key) }
}
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
import straightway.peerspace.crypto.CryptoIdentity
import straightway.peerspace.crypto.SignatureChecker
import straightway.peerspace.crypto.Signer
import java.io.Serializable
import java.security.KeyPair
import java.security.KeyPairGenerator

/**
 * Cryptor implementation encrypting payload keys using RSA2048, while the payload
 * itself is encrypted symmetrically.
 */
class RSA2048Cryptor private constructor(
        val keyPair: KeyPair,
        private val factory: CryptoFactory) :
        CryptoIdentity,
        Serializable,
        Signer by RSA2048Signer(keyPair.private, factory),
        SignatureChecker by RSA2048SignatureChecker(keyPair.public, factory) {

    constructor(factory: CryptoFactory) : this(
            KeyPairGenerator.getInstance(keyCipherAlgorithm)
                    .apply { initialize(RSA2048Cryptor.keyBits) }.genKeyPair(),
            factory)

    override val keyBits = RSA2048Cryptor.keyBits
    override val hashAlgorithm = factory.hashAlgorithm

    companion object {
        const val keyBits = 2048
        const val keyCipherAlgorithm = "RSA"
        const val serialVersionUID = 1L
    }
}
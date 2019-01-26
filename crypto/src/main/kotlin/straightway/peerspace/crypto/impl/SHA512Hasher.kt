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

import straightway.peerspace.crypto.Hasher
import java.security.MessageDigest

/**
 * Hasher implementaion using the SHA-512 hashing algorithm.
 */
class SHA512Hasher : Hasher {

    companion object {
        const val hashBits = 512
        const val hashAlgorithm = "SHA-512"
    }

    override val algorithm = hashAlgorithm.replace("-", "")
    override val hashBits = SHA512Hasher.hashBits

    override fun getHash(data: ByteArray) =
        MessageDigest.getInstance(hashAlgorithm).digest(data)
}
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

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

internal fun publicKeyFrom(rawKey: ByteArray, algorithm: String): PublicKey {
    val keyFactory = KeyFactory.getInstance(algorithm)
    return keyFactory.generatePublic(X509EncodedKeySpec(rawKey))
}

internal fun privateKeyFrom(rawKey: ByteArray, algorithm: String) =
        KeyFactory.getInstance(algorithm)!!.generatePrivate(PKCS8EncodedKeySpec(rawKey))!!
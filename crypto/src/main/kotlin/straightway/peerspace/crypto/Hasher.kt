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

import straightway.error.Panic
import straightway.utils.toByteArray
import java.io.Serializable

/**
 * Compute hash codes for data arrays.
 */
interface Hasher {
    val algorithm: String
    val hashBits: Int
    fun getHash(data: ByteArray): ByteArray
}

fun Hasher.getHash(obj: Serializable) = getHash(obj.toByteArray())
val Hasher.hashBytes get() =
    if (hashBits <= 0) throw Panic("hashBits must be positive (got: $hashBits)")
    else (hashBits - 1) / Byte.SIZE_BITS + 1
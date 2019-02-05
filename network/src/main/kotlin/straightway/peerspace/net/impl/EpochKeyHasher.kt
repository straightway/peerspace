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

package straightway.peerspace.net.impl

import straightway.peerspace.crypto.getHash
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.data.untimedData
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.epochAnalyzer
import straightway.peerspace.net.hasher

/**
 * Hasher computing hash codes for KeyHashable objects, respecting the timestamp
 * range of these objects and assigning them to time epochs (instead of using the
 * timestamps directly for hashing).
 */
class EpochKeyHasher : KeyHasher, PeerComponent by PeerComponent() {

    override fun getHashes(hashable: KeyHashable) =
            when (hashable.timestamps) {
                untimedData -> hashable.dataHashes
                else -> hashable.epochHashes
            }

    private val KeyHashable.epochHashes get() =
            epochs.map { getLongHash("EPOCH$it") }

    private val KeyHashable.epochs get() =
            epochAnalyzer.getEpochs(timestamps)

    private val KeyHashable.dataHashes get() =
            listOf(getLongHash("DATA"))

    private fun KeyHashable.getLongHash(hashType: String) =
            foldToLong(hasher.getHash("$hashType($id)"))

    companion object {
        private const val BitsPerByte = 8
        private fun foldToLong(array: ByteArray): Long = array.foldIndexed(0L) {
            index, result, byte -> result or (byte.toLong() shl
                ((index % BitsPerByte) * BitsPerByte))
        }
    }
}
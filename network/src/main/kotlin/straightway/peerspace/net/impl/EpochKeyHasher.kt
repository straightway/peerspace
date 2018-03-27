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

import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.net.mostRecentData
import straightway.peerspace.net.untimedData
import straightway.utils.TimeProvider
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Hasher computing hash codes for KeyHashable objects, respecting the timestamp
 * range of these objects and assigning them to time epochs (instead of using the
 * timestamps directly for hashing).
 */
class EpochKeyHasher(
        private val hasher: Hasher,
        private val timeProvider: TimeProvider,
        private val epochs: Array<LongRange>) : KeyHasher {

    override fun getHashes(hashable: KeyHashable) =
            when (hashable.timestamps) {
                untimedData -> getDataHashes(hashable)
                mostRecentData -> getMostRecentHashes(hashable)
                else -> getEpochHashes(hashable)
            }

    private fun getEpochHashes(hashable: KeyHashable) =
            getEpochs(hashable.timestamps).map {
                foldToLong(hasher.getHash("EPOCH$it(${hashable.id})"))
            }

    private fun getMostRecentHashes(hashable: KeyHashable) =
            listOf(foldToLong(hasher.getHash("RECENT(${hashable.id})")))

    private fun getDataHashes(hashable: KeyHashable) =
            listOf(foldToLong(hasher.getHash("DATA(${hashable.id})")))

    private fun getEpochs(timestamp: ClosedRange<Long>) =
            getRelativeRange(timestamp).let {
                ageRange -> epochs.indices.filter { ageRange overlapsWith epochs[it] }
            }

    private fun getRelativeRange(timestamp: ClosedRange<Long>) =
            (currentTimeStamp - timestamp.endInclusive)..(currentTimeStamp - timestamp.start)

    private infix fun ClosedRange<Long>.overlapsWith(outer: ClosedRange<Long>) =
            start in outer ||
            endInclusive in outer ||
            start < outer.start && outer.endInclusive < endInclusive

    private val currentTimeStamp get() =
        ChronoUnit.MILLIS.between(LocalDateTime.of(0, 1, 1, 0, 0), timeProvider.currentTime)

    companion object {
        private const val BitsPerByte = 8
        private fun foldToLong(array: ByteArray): Long = array.foldIndexed(0L) {
            index, result, byte -> result or (byte.toLong() shl
                ((index % BitsPerByte) * BitsPerByte))
        }
    }
}
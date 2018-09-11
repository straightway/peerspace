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

import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.net.EpochAnalyzer
import straightway.peerspace.net.timeProvider
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Default implementation of the EpochAnalyzer interface.
 */
class EpochAnalyzerImpl(
    private val epochs: Array<LongRange>
) : EpochAnalyzer, KoinModuleComponent by KoinModuleComponent() {

    override fun getEpochs(timestamp: ClosedRange<Long>) =
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
        ChronoUnit.MILLIS.between(LocalDateTime.of(0, 1, 1, 0, 0), timeProvider.now)
}
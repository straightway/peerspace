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
package straightway.peerspace.networksimulator.user

import straightway.error.Panic
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.random.UniformDoubleDistribution0to1
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.units.plus
import straightway.units.times

/**
 * Timing of an activity within a series of time ranges.
 */
class ActivityTimingImpl(
        private val ranges: List<TimeRange>,
        private val duration: UnitNumber<Time>
) : ActivityTiming, KoinModuleComponent by KoinModuleComponent() {

    // region Component references

    private val randomSource: Iterator<Byte> by inject("randomSource")

    // endregion

    // region Fields

    private val randomNumberGenerator by lazy { UniformDoubleDistribution0to1(randomSource) }

    // endregion

    override val timeRange by lazy { startTime..endTime }

    // region Private

    private val startTime: UnitNumber<Time> by lazy { unifiedRanges.findTime(relativeStartTime) }
    private val unifiedRanges by lazy { getUnifiedRanges(ranges) }
    private fun getUnifiedRanges(ranges: List<TimeRange>): List<TimeRange> =
            when {
                ranges.size < 2 ->
                    ranges
                ranges[1].start <= ranges[0].endInclusive ->
                    getUnifiedRanges(
                            listOf(ranges[0].start..ranges[1].endInclusive) + ranges.drop(2))
                else ->
                    ranges.slice(0..0) + getUnifiedRanges(ranges.drop(1))
            }

    private val relativeStartTime by lazy { activityRange * randomNumberGenerator.next() }
    private val endTime get() = startTime + duration
    private val activityRange: UnitNumber<Time> get() =
        unifiedRanges.fold(0[hour] as UnitNumber<Time>) {
            acc, range -> acc + range.size - duration
        }

    private fun List<TimeRange>.findTime(
            relativeTimeOverAllRanges: UnitNumber<Time>
    ): UnitNumber<Time> =
            with(first()) {
                val startRangeSize = size - duration
                if (relativeTimeOverAllRanges <= startRangeSize) start + relativeTimeOverAllRanges
                else drop(1).findTime(relativeTimeOverAllRanges - startRangeSize)
            }

    private val TimeRange.size get() = endInclusive - start

    init {
        if (ranges.isEmpty()) throw Panic("No ranges specified")
    }

    // endregion
}

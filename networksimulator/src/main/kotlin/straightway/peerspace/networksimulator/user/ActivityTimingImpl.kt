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
        private val ranges: TimeRanges,
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

    private val startTime: UnitNumber<Time> by lazy {
        ranges.findRelativeSubRange(relativeStartTime, duration)
    }

    private val relativeStartTime by lazy { activityRange * randomNumberGenerator.next() }
    private val endTime get() = startTime + duration
    private val activityRange: UnitNumber<Time> get() =
        ranges.fold(0[hour] as UnitNumber<Time>) { acc, range ->
            acc + range.endInclusive - range.start - duration
        }

    private fun TimeRanges.findRelativeSubRange(
            relativeTimeOverAllRanges: UnitNumber<Time>,
            duration: UnitNumber<Time>
    ): UnitNumber<Time> {
        var rest = relativeTimeOverAllRanges
        forEach {
            val size = it.endInclusive - it.start
            val startRangeSize = size - duration
            if (rest <= startRangeSize) return it.start + rest
            rest -= startRangeSize
        }
        throw DoesNotFitException("Activity of duration $duration does not fit into $ranges")
    }

    init {
        if (!ranges.any()) throw Panic("No ranges specified")
    }

    // endregion
}

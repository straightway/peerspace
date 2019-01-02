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
import straightway.units.UnitDouble
import straightway.units.get
import straightway.units.hour

/**
 * Timing of an activity within a series of time ranges.
 */
class ActivityTimingImpl(
        private val ranges: TimeRanges,
        private val duration: UnitDouble<Time>
) : ActivityTiming, KoinModuleComponent by KoinModuleComponent() {

    // region Component references

    private val randomSource: Iterator<Byte> by inject("randomSource")

    // endregion

    // region Fields

    private val randomNumberGenerator by lazy { UniformDoubleDistribution0to1(randomSource) }

    // endregion

    override val timeRange: TimeRange by lazy { startTime..endTime }

    // region Private

    private val startTime: UnitDouble<Time> by lazy {
        fittingRanges.findRelativeSubRange(relativeStartTime, duration)
    }

    private val relativeStartTime by lazy { activityRange * randomNumberGenerator.next() }
    private val endTime get() = startTime + duration
    private val activityRange: UnitDouble<Time> get() =
        fittingRanges.fold(0.0[hour]) { acc, range ->
                  acc + range.size - duration
              }
    private val fittingRanges = ranges.filter { duration <= it.size }
    private val TimeRange.size get() = endInclusive - start

    private fun Iterable<TimeRange>.findRelativeSubRange(
            relativeTimeOverAllRanges: UnitDouble<Time>,
            duration: UnitDouble<Time>
    ): UnitDouble<Time> {
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

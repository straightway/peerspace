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

import straightway.units.Time
import straightway.units.UnitDouble
import straightway.units.get
import straightway.units.hour
import straightway.units.second
import straightway.utils.Ranges

/**
 * Optimized collection of ranges, spezialized for time ranges.
 */
class TimeRanges(initRanges: Iterable<TimeRange>) : Iterable<TimeRange> {

    constructor() : this (listOf())

    override fun iterator() = ranges.map { it.start[second]..it.endInclusive[second] }.iterator()

    operator fun minusAssign(r: TimeRange) = ranges.minusAssign(r.prim)

    operator fun plusAssign(r: TimeRange) = ranges.plusAssign(r.prim)

    override fun toString() =
            "TimeRanges[${map { it.start[hour]..it.endInclusive[hour] }.joinToString(", ")}]"

    val size: Int get() = ranges.size

    companion object {
        operator fun invoke(vararg initRanges: ClosedRange<UnitDouble<Time>>) =
                TimeRanges(initRanges.map { it.start..it.endInclusive })
        private val TimeRange.prim get() =
            start.baseValue.toDouble()..endInclusive.baseValue.toDouble()
    }

    private var ranges = Ranges(initRanges.map { it.prim })
}
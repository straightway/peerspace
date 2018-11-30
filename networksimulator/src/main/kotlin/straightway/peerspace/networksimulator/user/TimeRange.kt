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
import straightway.units.UnitNumber
import straightway.units.max
import straightway.units.min

typealias TimeRange = ClosedRange<UnitNumber<Time>>

fun List<TimeRange>.exclude(toExclude: TimeRange): List<TimeRange> =
        when {
            toExclude.isEmpty -> this
            isEmpty() -> listOf()
            else -> first().exclude(toExclude) + drop(1).exclude(toExclude)
        }

fun List<TimeRange>.include(toInclude: TimeRange): List<TimeRange> =
        when {
            isEmpty() -> listOf(toInclude)
            else -> includeInNonEmptyRangeList(toInclude)
        }

private fun List<TimeRange>.includeInNonEmptyRangeList(toInclude: TimeRange) =
        drop(1).includeInNonEmptyRangeList(first(), toInclude)

private fun List<TimeRange>.includeInNonEmptyRangeList(
        firstRange: TimeRange, toInclude: TimeRange
) =
    firstRange.include(toInclude).let { included ->
        if (included.any()) included + this
        else listOf(firstRange) + include(toInclude)
    }

private fun TimeRange.include(include: TimeRange): List<TimeRange> =
        when {
            include.start in this && include.endInclusive in this ->
                listOf(this)
            start in include || endInclusive in include ->
                listOf(min(start, include.start)..max(endInclusive, include.endInclusive))
            start < include.start ->
                listOf()
            else ->
                listOf(include, this)
        }

private fun TimeRange.exclude(exclude: TimeRange) =
        when {
            isEmpty -> listOf()
            exclude.start in this -> partBefore(exclude.start) + partAfter(exclude.endInclusive)
            exclude.endInclusive in this -> partAfter(exclude.endInclusive)
            else -> listOf(this)
        }

private val TimeRange.isEmpty: Boolean get() =
        endInclusive <= start

private fun TimeRange.partBefore(border: UnitNumber<Time>) =
        if (start < border) listOf(start..border) else listOf()

private fun TimeRange.partAfter(border: UnitNumber<Time>) =
        if (border < endInclusive) listOf(border..endInclusive) else listOf()
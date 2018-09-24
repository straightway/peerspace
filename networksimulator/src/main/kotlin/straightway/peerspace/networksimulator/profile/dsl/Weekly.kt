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
package straightway.peerspace.networksimulator.profile.dsl

import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.get
import straightway.units.hour
import straightway.units.nano
import straightway.units.second
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Define a time span on a defined set of days every week.
 */
@Suppress("MagicNumber")
class Weekly(private val weekdayFilterName: String, init: Weekly.() -> Unit) {
    companion object {
        val mondays get() = Weekly("mondays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.MONDAY } }
        }
        val tuesdays get() = Weekly("tuesdays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.TUESDAY } }
        }
        val wednesdays get() = Weekly("wednesdays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.WEDNESDAY } }
        }
        val thursdays get() = Weekly("thursdays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.THURSDAY } }
        }
        val fridays get() = Weekly("fridays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.FRIDAY } }
        }
        val saturdays get() = Weekly("saturdays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.SATURDAY } }
        }
        val sundays get() = Weekly("sundays") {
            isApplicableTo { { it.dayOfWeek == DayOfWeek.SUNDAY } }
        }
        val workdays get() = Weekly("workdays") {
            isApplicableTo { { it.dayOfWeek.value in 1..5 } }
        }
        val weekends get() = Weekly("weekends") {
            isApplicableTo { { it.dayOfWeek.value in 6..7 } }
        }
        val eachDay get() = Weekly("daily") {
            isApplicableTo { { true } }
        }
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    val isApplicableTo = SingleValueProvider<(LocalDateTime) -> Boolean>("isApplicableTo")
    fun isApplicableTo(dateTime: LocalDateTime) = isApplicableTo.value(dateTime)

    val hours = SingleValueProvider<ClosedRange<UnitNumber<Time>>>("hours")

    override fun toString() = (listOf(weekdayFilterName) + hoursString).joinToString(" ")

    operator fun invoke(valueGetter: Weekly.() -> ClosedRange<UnitNumber<Time>>): Weekly {
        hours {
            @Suppress("UNUSED_EXPRESSION")
            valueGetter()
        }
        return this
    }

    init {
        hours { 0[hour]..24[hour] }
        @Suppress("UNUSED_EXPRESSION")
        init()
    }

    private val hoursString get() = with(hours.value) {
        if (this == 0[hour]..24[hour]) listOf()
        else {
            val from = start.toTimeString()
            val to = endInclusive.toTimeString()
            listOf("$from..$to")
        }
    }

    private fun UnitNumber<Time>.toTimeString() =
            toLocalTime().format(timeFormatter)

    private fun UnitNumber<Time>.toLocalTime() =
            LocalTime.MIDNIGHT.plusNanos(this[nano(second)].value.toLong() + 1)
}
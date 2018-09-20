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
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Define a time span on a defined set of days every week.
 */
class Weekly(init: Weekly.() -> Unit) {
    companion object {
        val mondays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.MONDAY } } }
        val tuesdays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.TUESDAY } } }
        val wednesdays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.WEDNESDAY } } }
        val thursdays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.THURSDAY } } }
        val fridays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.FRIDAY } } }
        val saturdays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.SATURDAY } } }
        val sundays get() = Weekly { isApplicableTo { { it.dayOfWeek == DayOfWeek.SUNDAY } } }
        val workdays get() = Weekly { isApplicableTo { { it.dayOfWeek.value in 1..5 } } }
        val weekends get() = Weekly { isApplicableTo { { it.dayOfWeek.value in 6..7 } } }
        val eachDay get() = Weekly { isApplicableTo { { true } } }
    }

    var isApplicableTo = SingleValueProvider<(LocalDateTime) -> Boolean>("isApplicableTo")
    fun isApplicableTo(dateTime: LocalDateTime) = isApplicableTo.value(dateTime)

    var hours = SingleValueProvider<ClosedRange<UnitNumber<Time>>>("hours")

    operator fun invoke(valueGetter: Weekly.() -> ClosedRange<UnitNumber<Time>>): Weekly {
        hours {
            @Suppress("UNUSED_EXPRESSION")
            valueGetter()
        }
        return this
    }

    init {
        @Suppress("UNUSED_EXPRESSION")
        init()
    }
}
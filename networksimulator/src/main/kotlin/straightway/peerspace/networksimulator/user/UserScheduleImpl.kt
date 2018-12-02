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

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.utils.TimeProvider
import java.time.LocalDate

/**
 * Default implementation of the UserSchedule interface.
 */
class UserScheduleImpl : UserSchedule, KoinModuleComponent by KoinModuleComponent() {

    // region Referenced components

    private val timeProvider: TimeProvider by inject()

    // endregion

    // region UserSchedule

    override fun getBlockedTimes(day: LocalDate): List<TimeRange> {
        clearPastDays()
        return blocked[day] ?: listOf()
    }

    override fun block(day: LocalDate, range: TimeRange) {
        when {
            range.endInclusive <= range.start -> Unit
            range.endInclusive < 0[hour] -> Unit
            range.start < 0[hour] -> block(day, 0[hour]..range.endInclusive)
            else -> blocked += mapOf(day to (blocked[day] ?: listOf()).include(range))
        }

        blockNextOverlappingDays(range, day)
    }

    // endregion

    // region Private

    private fun clearPastDays() {
        val today = timeProvider.now.toLocalDate()
        blocked -= blocked.keys.filter { it < today }
    }

    private fun blockNextOverlappingDays(range: TimeRange, day: LocalDate) {
        if (fullDay < range.endInclusive)
            block(day.plusDays(1), (range.start - fullDay)..(range.endInclusive - fullDay))
    }

    private var blocked = mapOf<LocalDate, List<TimeRange>>()

    private companion object {
        val fullDay = 1[straightway.units.day]
    }

    // endregion
}
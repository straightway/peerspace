// ktlint-disable filename
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
import straightway.sim.Scheduler
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.units.plus
import straightway.utils.TimeProvider
import straightway.utils.max
import straightway.utils.min
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Default implementation of the UserActivityScheduler interface.
 */
class UserActivitySchedulerImpl :
        UserActivityScheduler, KoinModuleComponent by KoinModuleComponent() {

    private companion object {
        val fullDay = 24[hour]
    }

    private val simScheduler: Scheduler by inject()
    private val timeProvider: TimeProvider by inject()
    private val user: User by inject()

    fun scheduleDay(day: LocalDate) {
        if (LocalDateTime.of(day, LocalTime.MIDNIGHT) < timeProvider.now)
            return
        scheduleNextDay(day)
        scheduleOnlineTimes(day)
    }

    private fun scheduleNextDay(day: LocalDate) =
        scheduleAt(day.at(fullDay)) { scheduleDay(day.plusDays(1)) }

    private fun scheduleOnlineTimes(day: LocalDate) {
        val newOnlineTimes = onlineTimesFor(day).filter {
            day.at(0[hour]) <= it.endInclusive
        }
        newOnlineTimes.filter { it.start.toLocalDate() == day }.forEach {
            scheduleOnlineTime(it)
        }
        scheduledOnlineTimes = newOnlineTimes
    }

    private fun onlineTimesFor(day: LocalDate) =
        mergeOverlaps(
                day,
                scheduledOnlineTimes + deviceUsages.single().onlineTimes.values.map {
                    day.at(it.hours.value)
                })

    private fun mergeOverlaps(day: LocalDate, times: Iterable<ClosedRange<LocalDateTime>>) =
            times.filter {
                it.start.toLocalDate() == day || it.endInclusive.toLocalDate() == day
            }.fold(listOf<ClosedRange<LocalDateTime>>()) { result, range ->
                result.addTimeRange(range)
            }

    private fun List<ClosedRange<LocalDateTime>>.addTimeRange(
            newRange: ClosedRange<LocalDateTime>): List<ClosedRange<LocalDateTime>> =
            firstOrNull { it.intersectsWith(newRange) }.let { intersection ->
                if (intersection == null) {
                    this + newRange
                } else {
                    (this - intersection).addTimeRange(newRange + intersection)
                }
            }

    private fun <T : Comparable<T>> ClosedRange<T>.intersectsWith(other: ClosedRange<T>) =
            contains(other.start) or contains(other.endInclusive)

    private operator fun <T : Comparable<T>> ClosedRange<T>.plus(
            other: ClosedRange<T>) =
            min(start, other.start)..max(endInclusive, other.endInclusive)

    private fun scheduleOnlineTime(onlineTimeRange: ClosedRange<LocalDateTime>) {
        if (onlineTimeRange.endInclusive <= onlineTimeRange.start)
            return
        if (onlineTimeRange.start < timeProvider.now)
            return
        scheduleAt(onlineTimeRange.start) {
            user.environment.devices.single().isOnline = true
            setOfflineIfTimeHasCome(onlineTimeRange)
        }
    }

    private fun setOfflineIfTimeHasCome(onlineTimeRange: ClosedRange<LocalDateTime>) {
        if (timeProvider.now == onlineTimeRange.offlineTime) {
            user.environment.devices.single().isOnline = false
        } else {
            scheduleAt(onlineTimeRange.offlineTime) {
                setOfflineIfTimeHasCome(onlineTimeRange)
            }
        }
    }

    private val ClosedRange<LocalDateTime>.offlineTime get() =
            scheduledOnlineTimes.single {
                it.start == start
            }.endInclusive

    private val deviceUsages get() = user.profile.usedDevices.values

    private fun LocalDate.at(time: UnitNumber<Time>) =
            LocalDateTime.of(this, LocalTime.MIDNIGHT) + time

    private fun LocalDate.at(range: ClosedRange<UnitNumber<Time>>) =
            at(range.start)..at(range.endInclusive)

    private fun scheduleAt(time: LocalDateTime, action: () -> Unit) =
        simScheduler.schedule(time - timeProvider.now, action)

    private var scheduledOnlineTimes = listOf<ClosedRange<LocalDateTime>>()
}
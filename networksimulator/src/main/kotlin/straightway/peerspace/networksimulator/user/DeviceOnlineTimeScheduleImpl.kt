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
import straightway.units.at
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.utils.TimeProvider
import straightway.utils.max
import straightway.utils.min
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Default implementstion of the DeviceOnlineTimeSchedule interace.
 */
class DeviceOnlineTimeScheduleImpl(val device: Device)
    : DeviceOnlineTimeSchedule, KoinModuleComponent by KoinModuleComponent() {

    // region Component references

    private val simScheduler: Scheduler by inject()
    private val timeProvider: TimeProvider by inject()

    // endregion

    // region fields

    private var scheduledOnlineTimes = listOf<ClosedRange<LocalDateTime>>()

    // endregion

    override fun scheduleOnlineTimes(day: LocalDate) {
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
                    scheduledOnlineTimes + definedOnlineTimesFor(day).map {
                        day.at(it.hours.value)
                    })

    private fun definedOnlineTimesFor(day: LocalDate) =
            device.usage.onlineTimes.values.filter {
                it.isApplicableTo.value(day.at(0[hour]))
            }

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

    private fun scheduleOnlineTime(onlineTimeRange: ClosedRange<LocalDateTime>) {
        if (onlineTimeRange.endInclusive <= onlineTimeRange.start)
            return
        if (onlineTimeRange.start < timeProvider.now)
            return
        scheduleAt(onlineTimeRange.start) {
            device.isOnline = true
            setOfflineIfTimeHasCome(onlineTimeRange)
        }
    }

    private fun setOfflineIfTimeHasCome(onlineTimeRange: ClosedRange<LocalDateTime>) {
        if (timeProvider.now == onlineTimeRange.offlineTime) {
            device.isOnline = false
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

    private fun <T : Comparable<T>> ClosedRange<T>.intersectsWith(other: ClosedRange<T>) =
            contains(other.start) or contains(other.endInclusive)

    private operator fun <T : Comparable<T>> ClosedRange<T>.plus(
            other: ClosedRange<T>) =
            min(start, other.start)..max(endInclusive, other.endInclusive)

    private fun scheduleAt(time: LocalDateTime, action: () -> Unit) =
            simScheduler.schedule(time - timeProvider.now, action)

    private fun LocalDate.at(range: TimeRange) =
            at(range.start)..at(range.endInclusive)
}
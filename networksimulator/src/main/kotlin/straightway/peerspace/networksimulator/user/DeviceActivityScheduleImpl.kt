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

import straightway.koinutils.Bean.get
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.networksimulator.profile.dsl.UsageProfile
import straightway.sim.Scheduler
import straightway.units.at
import straightway.units.minus
import straightway.utils.TimeProvider
import straightway.utils.max
import straightway.utils.min
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Default implementation of the DeviceActivitySchedule interface.
 */
class DeviceActivityScheduleImpl(val device: Device)
    : DeviceActivitySchedule, KoinModuleComponent by KoinModuleComponent() {

    // region Component references

    private val simScheduler: Scheduler by inject()
    private val timeProvider: TimeProvider by inject()

    // endregion

    // region DeviceActivitySchedule implementation

    override fun scheduleActivities(day: LocalDate) =
        device.usage.usages.values.forEach { it.scheduleActivityAt(day) }

    // endregion

    // region Private

    private fun UsageProfile.scheduleActivityAt(day: LocalDate) {
        var timeRanges = listOf(time.value.hours.value)
        (1..numberOfTimes.value).forEach { _ ->
            with(activityTiming(timeRanges)) {
                timeRanges = timeRanges.exclude(timeRange)
                scheduleAt(day.at(timeRange.endInclusive)) {
                    activity.value.action(device, this@scheduleActivityAt)
                }
            }
        }
    }

    private fun UsageProfile.activityTiming(timeRanges: List<TimeRange>) = get<ActivityTiming> {
        mapOf(
                "ranges" to timeRanges,
                "duration" to duration.value)
    }

    private operator fun <T : Comparable<T>> ClosedRange<T>.plus(
            other: ClosedRange<T>) =
            min(start, other.start)..max(endInclusive, other.endInclusive)

    private fun scheduleAt(time: LocalDateTime, action: () -> Unit) =
            simScheduler.schedule(time - timeProvider.now, action)

    // endregion
}
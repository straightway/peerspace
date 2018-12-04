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

import straightway.koinutils.Bean.get
import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.sim.Scheduler
import straightway.units.at
import straightway.units.get
import straightway.units.hour
import straightway.units.minus
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Default implementation of the UserActivityScheduler interface.
 */
class UserActivitySchedulerImpl :
        UserActivityScheduler, KoinModuleComponent by KoinModuleComponent() {

    // region Constants

    private companion object {
        val fullDay = 24[hour]
    }

    // endregion

    // region Component references

    private val simScheduler: Scheduler by inject()
    private val timeProvider: TimeProvider by inject()
    private val user: User by inject()
    private val userSchedule: UserSchedule by inject()

    // endregion

    // region Fields

    private val deviceOnlineTimeSchedule = user.environment.devices.map {
        get<DeviceOnlineTimeSchedule> { mapOf("device" to it) }
    }

    private val deviceActivitySchedule = user.environment.devices.map {
        get<DeviceActivitySchedule> { mapOf("device" to it) }
    }

    // endregion

    // region UserActivityScheduler implementation

    override fun scheduleDay(day: LocalDate) {
        if (LocalDateTime.of(day, LocalTime.MIDNIGHT) < timeProvider.now)
            return
        blockInactiveTimes(day)
        scheduleActivityEvents(day)
    }

    // endregion

    // region Private

    private fun scheduleActivityEvents(day: LocalDate) {
        scheduleNextDay(day)
        scheduleOnlineTimes(day)
        scheduleActivities(day)
    }

    private fun blockInactiveTimes(day: LocalDate) =
            getInactivityTimes(day).forEach { userSchedule.block(day, it) }

    private fun getInactivityTimes(day: LocalDate): List<TimeRange> {
        var inactivityTimes = listOf<TimeRange>(0[hour]..fullDay)
        getActivityTimesFor(day).forEach {
            inactivityTimes = inactivityTimes.exclude(it.hours.value)
        }
        return inactivityTimes
    }

    private fun getActivityTimesFor(day: LocalDate) =
            activityTimes.filter { it.isApplicableTo(day.at(0[hour])) }

    private val activityTimes get() = user.profile.activityTimes.values

    private fun scheduleNextDay(day: LocalDate) =
            scheduleAt(day.at(fullDay)) { scheduleDay(day.plusDays(1)) }

    private fun scheduleOnlineTimes(day: LocalDate) =
            deviceOnlineTimeSchedule.forEach { it.scheduleOnlineTimes(day) }

    private fun scheduleActivities(day: LocalDate) =
            deviceActivitySchedule.forEach { it.scheduleActivities(day) }

    private fun scheduleAt(time: LocalDateTime, action: () -> Unit) =
            simScheduler.schedule(time - timeProvider.now, action)

    // endregion
}

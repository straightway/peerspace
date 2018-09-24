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
@file:Suppress("MatchingDeclarationName")
package straightway.peerspace.networksimulator.user

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.sim.Scheduler
import straightway.units.minus
import straightway.units.plus
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Default implementation of the UserActivityScheduler interface.
 */
class UserActivitySchedulerImpl :
        UserActivityScheduler, KoinModuleComponent by KoinModuleComponent() {

    private val simScheduler: Scheduler by inject()
    private val timeProvider: TimeProvider by inject()
    private val user: User by inject()

    private val deviceUsages = user.profile.usedDevices.values

    @Suppress("UNUSED_PARAMETER")
    fun scheduleDay(day: LocalDate) {
        val onlineTime = deviceUsages.single().onlineTimes.values.single().hours.value
        val timeDiff =
                LocalDateTime.of(day, LocalTime.MIDNIGHT) -
                LocalDateTime.of(timeProvider.now.toLocalDate(), LocalTime.MIDNIGHT)
        simScheduler.schedule(timeDiff + onlineTime.start) {}
    }
}
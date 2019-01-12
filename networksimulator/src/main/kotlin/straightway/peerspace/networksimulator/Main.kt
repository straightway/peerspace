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
package straightway.peerspace.networksimulator

import straightway.koinutils.Bean.get
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.Id
import straightway.koinutils.withContext
import straightway.sim.net.Network as SimNetwork
import straightway.peerspace.networksimulator.profile.officeWorker
import straightway.peerspace.networksimulator.user.ActivityTiming
import straightway.peerspace.networksimulator.user.ActivityTimingImpl
import straightway.peerspace.networksimulator.user.Device
import straightway.peerspace.networksimulator.user.DeviceActivitySchedule
import straightway.peerspace.networksimulator.user.DeviceActivityScheduleImpl
import straightway.peerspace.networksimulator.user.DeviceImpl
import straightway.peerspace.networksimulator.user.DeviceOnlineTimeSchedule
import straightway.peerspace.networksimulator.user.DeviceOnlineTimeScheduleImpl
import straightway.peerspace.networksimulator.user.InterconnectedGroupOfUsers
import straightway.peerspace.networksimulator.user.User
import straightway.peerspace.networksimulator.user.UserActivityScheduler
import straightway.peerspace.networksimulator.user.UserActivitySchedulerImpl
import straightway.peerspace.networksimulator.user.UserImpl
import straightway.peerspace.networksimulator.user.UserSchedule
import straightway.peerspace.networksimulator.user.UserScheduleImpl
import straightway.random.Chooser
import straightway.random.RandomChooser
import straightway.random.RandomSource
import straightway.sim.core.InterceptingScheduler
import straightway.sim.core.Simulator
import straightway.units.at
import straightway.units.get
import straightway.units.hour
import straightway.units.milli
import straightway.units.second
import straightway.units.minus
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Random

private const val FULL_DAY_HOURS = 24.0

@Suppress("LargeClass")
private class MainClass(
        numberOfUsers: Int,
        randomSeed: Long,
        startDate: LocalDate,
        val verbose: Boolean
) {

    val simulator = Simulator()

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY,
            offlineDetectionTime = OFFLINE_DETECTION_TIME)

    private val simNodes = mutableMapOf<Id, SimNode>()

    private val randomSource = RandomSource(Random(randomSeed))

    val userContexts = (1..numberOfUsers).map {
            withContext {
                bean("simNodes") { simNodes }
                bean("knownUsers") { mutableListOf<User>() }
                bean { officeWorker }
                bean("randomSource") { randomSource as Iterator<Byte> }
                bean { simulator as TimeProvider }
                bean {
                    if (verbose) InterceptingScheduler(simulator).onExecuted { action ->
                        println("${simulator.now}: $action")
                    }
                    else simulator
                }
                bean { simNet }
                bean { UserActivitySchedulerImpl() as UserActivityScheduler }
                bean { UserImpl() as User }
                bean { UserScheduleImpl() as UserSchedule }
                factory { args ->
                    DeviceImpl(args["id"], args["profile"]) as Device
                }
                factory { args ->
                    ActivityTimingImpl(args["ranges"], args["duration"]) as ActivityTiming
                }
                factory { args ->
                    DeviceActivityScheduleImpl(args["device"]) as DeviceActivitySchedule
                }
                factory { args ->
                    DeviceOnlineTimeScheduleImpl(args["device"]) as DeviceOnlineTimeSchedule
                }
            } make {
                KoinModuleComponent()
            }
        }

    @Suppress("LongMethod")
    fun initializeSimulation() {
        println("Initializing simulation at ${simulator.now}")
        introduceUsersToEachOther()
        scheduleInitialEventsForEachUser()
        if (!verbose) scheduleEndOfDayMessage()
        println("done.")
    }

    init {
        simulator.schedule(
                LocalDateTime.of(startDate, LocalTime.MIDNIGHT) - simulator.now,
                "Global initialization",
                this::initializeSimulation)
    }

    private fun scheduleEndOfDayMessage() {
        val endOfDay = simulator.now.toLocalDate().at(FULL_DAY_HOURS[hour])
        simulator.schedule(endOfDay - simulator.now, "day done") {
            println("$endOfDay done")
            scheduleEndOfDayMessage()
        }
    }

    private fun scheduleInitialEventsForEachUser() {
        userContexts.forEach { context ->
            val userScheduler = context.get<UserActivityScheduler>()
            userScheduler.scheduleDay(simulator.now.toLocalDate())
            print(".")
        }
        println()
    }

    private fun introduceUsersToEachOther() {
        val chooser = RandomChooser(randomSource)
        val userGroups = userContexts.map {
            InterconnectedGroupOfUsers(chooser, listOf(it.get()))
        }
        unifyAll(chooser, userGroups)
        printUsersAndFriends()
    }

    private fun printUsersAndFriends() {
        if (!verbose) return
        println("Known users:")
        userContexts.forEach { ctx ->
            println("${ctx.get<User>().id} knows " +
                    ctx.get<List<User>>("knownUsers").map { it.id }.joinToString(", "))
        }
    }

    private fun unifyAll(chooser: Chooser, groups: List<InterconnectedGroupOfUsers>) {
        when (groups.size) {
            0, 1 -> return
            else -> {
                val candidates = chooser.chooseFrom(groups, 2)
                val unified = candidates.first().unifyWith(candidates.last())
                unifyAll(chooser, groups - candidates + unified)
            }
        }
    }

    private companion object {
        val LATENCY = 50.0[milli(second)]
        val OFFLINE_DETECTION_TIME = 5.0[second]
    }
}

@Suppress("UNUSED_VARIABLE", "MagicNumber")
fun main(args: Array<String>) {
    KoinLoggingDisabler().use {
        println("Starting simulation")

        val mainClass = MainClass(
                numberOfUsers = 100,
                randomSeed = 1234L,
                startDate = LocalDate.of(2023, 2, 3),
                verbose = args.any { arg -> arg == "-v" })
        println("Created ${mainClass.userContexts.size} userContexts")
        mainClass.simulator.run()

        println("Simulation finished")
    }
}
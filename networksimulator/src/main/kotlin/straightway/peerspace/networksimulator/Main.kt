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
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.networksimulator.profile.officeWorker
import straightway.peerspace.networksimulator.user.ActivityTiming
import straightway.peerspace.networksimulator.user.ActivityTimingImpl
import straightway.peerspace.networksimulator.user.Device
import straightway.peerspace.networksimulator.user.DeviceActivitySchedule
import straightway.peerspace.networksimulator.user.DeviceActivityScheduleImpl
import straightway.peerspace.networksimulator.user.DeviceImpl
import straightway.peerspace.networksimulator.user.DeviceOnlineTimeSchedule
import straightway.peerspace.networksimulator.user.DeviceOnlineTimeScheduleImpl
import straightway.peerspace.networksimulator.user.User
import straightway.peerspace.networksimulator.user.UserActivityScheduler
import straightway.peerspace.networksimulator.user.UserActivitySchedulerImpl
import straightway.peerspace.networksimulator.user.UserSchedule
import straightway.peerspace.networksimulator.user.UserScheduleImpl
import straightway.random.RandomSource
import straightway.sim.core.InterceptingScheduler
import straightway.sim.core.Simulator
import straightway.units.byte
import straightway.units.get
import straightway.units.ki
import straightway.units.milli
import straightway.units.second
import straightway.units.minus
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Random

@Suppress("LargeClass")
private class MainClass(
        numberOfPeers: Int,
        randomSeed: Long,
        startDate: LocalDate,
        val verbose: Boolean
) {

    val simulator = Simulator()

    var initializationFinished = false

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY,
            offlineDetectionTime = OFFLINE_DETECTION_TIME)

    private val simNodes = mutableMapOf<Id, SimNode>()

    private val randomSource = RandomSource(Random(randomSeed))

    private val chunkSizeGetter = chunkSizeGetter { _ -> CHUNK_SIZE }

    val userContexts = (1..numberOfPeers).map { _ ->
            withContext {
                bean("simNodes") { simNodes }
                bean { _ -> officeWorker }
                bean("randomSource") { _ -> randomSource as Iterator<Byte> }
                bean { _ -> simulator as TimeProvider }
                bean { _ ->
                    if (verbose) InterceptingScheduler(simulator).onExecuted {
                        println("${simulator.now}: $it")
                    }
                    else simulator
                }
                bean { _ -> chunkSizeGetter }
                bean { _ -> simNet }
                bean { _ -> UserActivitySchedulerImpl() as UserActivityScheduler }
                bean { _ -> User() }
                bean { _ -> UserScheduleImpl() as UserSchedule }
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

    fun initializeSimulation() {
        println("Initializing simulation at ${simulator.now}")
        scheduleInitialEventsForEachUser()
        println("done.")
        initializationFinished = true
    }

    init {
        simulator.schedule(
                LocalDateTime.of(startDate, LocalTime.MIDNIGHT) - simulator.now,
                "Global initialization",
                this::initializeSimulation)
    }

    private fun scheduleInitialEventsForEachUser() {
        userContexts.forEach { context ->
            val userScheduler = context.get<UserActivityScheduler>()
            userScheduler.scheduleDay(simulator.now.toLocalDate())
            print(".")
        }
        println()
    }

    private companion object {
        val LATENCY = 50.0[milli(second)]
        val OFFLINE_DETECTION_TIME = 5.0[second]
        val CHUNK_SIZE = 64[ki(byte)]
    }
}

@Suppress("UNUSED_VARIABLE", "MagicNumber")
fun main(args: Array<String>) {
    KoinLoggingDisabler().use {
        println("Starting simulation")

        val mainClass = MainClass(
                numberOfPeers = 100,
                randomSeed = 1234L,
                startDate = LocalDate.of(2023, 2, 3),
                verbose = args.any { it == "-v" })
        println("Created ${mainClass.userContexts.size} userContexts")
        mainClass.simulator.run()

        println("Simulation finished")
    }
}
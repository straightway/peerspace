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
import straightway.peerspace.networksimulator.user.User
import straightway.peerspace.networksimulator.user.UserActivityScheduler
import straightway.peerspace.networksimulator.user.UserActivitySchedulerImpl
import straightway.random.RandomDistribution
import straightway.random.RandomSource
import straightway.sim.core.Simulator
import straightway.units.byte
import straightway.units.get
import straightway.units.ki
import straightway.units.milli
import straightway.units.second
import straightway.units.minus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Random

private class MainClass(numberOfPeers: Int, randomSeed: Long, startDate: LocalDate) {

    val simulator = Simulator()

    private val simNet = SimNetwork(
            simScheduler = simulator,
            timeProvider = simulator,
            latency = LATENCY,
            offlineDetectionTime = OFFLINE_DETECTION_TIME)

    private val simNodes = mutableMapOf<Id, SimNode>()

    private val randomSource = RandomSource(Random(randomSeed))

    private val chunkSizeGetter = chunkSizeGetter { _ -> CHUNK_SIZE }

    val users = (1..numberOfPeers).map { _ ->
            withContext {
                bean("simNodes") { simNodes }
                bean { _ -> officeWorker }
                bean("randomSource") { _ -> randomSource as RandomDistribution<Byte> }
                bean { _ -> simulator }
                bean { _ -> chunkSizeGetter }
                bean { _ -> simNet }
                bean { _ -> UserActivitySchedulerImpl() as UserActivityScheduler }
                bean { _ -> User() }
            } make {
                KoinModuleComponent().get<User>()
            }
        }

    fun initializeSimulation() {
        println("Initializing simulation at ${simulator.now}")
        users.forEach { }
    }

    init {
        simulator.schedule(
                LocalDateTime.of(startDate, LocalTime.MIDNIGHT) - simulator.now,
                this::initializeSimulation)
    }

    private companion object {
        val LATENCY = 50[milli(second)]
        val OFFLINE_DETECTION_TIME = 5[second]
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
                startDate = LocalDate.of(2023, 2, 3))
        println("Created ${mainClass.users.size} users")
        mainClass.simulator.run()

        println("Simulation finished")
    }
}
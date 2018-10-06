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
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.data.Id
import straightway.peerspace.networksimulator.profile.dsl.UserProfile

/**
 * A simulated peerspace user.
 */
@Suppress("MagicNumber")
class User : KoinModuleComponent by KoinModuleComponent() {

    val profile: UserProfile by inject()
    private val activityScheduler: UserActivityScheduler by inject()

    val environment: UserEnvironment = Environment()
    val id: Id = Id("User_${currentId++}")

    private inner class Environment : UserEnvironment {
        override val devices =
                profile.usedDevices.values.map {
                    get<Device> {
                        mapOf(
                            "id" to Id("Peer_${currentId++}"),
                            "usageProfile" to it)
                    }
                }
    }

    private companion object {
        var currentId = 0
    }
}

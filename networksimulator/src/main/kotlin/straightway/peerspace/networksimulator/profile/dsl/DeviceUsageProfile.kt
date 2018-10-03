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
package straightway.peerspace.networksimulator.profile.dsl

import straightway.utils.joinMultiLine

/**
 * Profile of how a certain device is used in the simulation.
 */
class DeviceUsageProfile(init: DeviceUsageProfile.() -> Unit) {

    val onlineTimes = StaticMultiValue<Weekly>("onlineTimes")
    val usages = StaticMultiValue<UsageProfile>("usages")
    val device = StaticSingleValue<DeviceProfile>("device")

    override fun toString() = "DeviceUsageProfile " +
            listOf(onlineTimes, usages, device).joinMultiLine(indentation = 2)

    init { init() }
}

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

import straightway.units.AmountOfData
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.utils.joinMultiLine

/**
 * Profile of how a certain activity is executed.
 */
class UsageProfile(init: UsageProfile.() -> Unit) {
    val activity = SingleValueProvider<Activity>("activity")
    val numberOfTimes = SingleValueProvider<Int>("numberOfTimes")
    val duration = SingleValueProvider<UnitNumber<Time>>("duration")
    val time = SingleValueProvider<Weekly>("time")
    val dataVolume = SingleValueProvider<UnitNumber<AmountOfData>>("dataVolume")

    operator fun invoke(update: UsageProfile.() -> Unit): UsageProfile { update(); return this }

    override fun toString() = "UsageProfile " +
            listOf(activity, numberOfTimes, duration, time, dataVolume)
                    .joinMultiLine(indentation = 2)

    init { init() }
}
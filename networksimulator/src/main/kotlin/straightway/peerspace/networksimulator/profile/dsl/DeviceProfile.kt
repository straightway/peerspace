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
import straightway.units.Bandwidth
import straightway.units.UnitNumber
import straightway.utils.joinMultiLine

/**
 * Profile for a simulated physical device participating in the peerspace network,
 * e.g. a PC or a smart phone.
 */
class DeviceProfile(init: DeviceProfile.() -> Unit) {
    val uploadBandwidth =
            SingleValueProvider<UnitNumber<Bandwidth>>("uploadBandwidth")
    val downloadBandwidth =
            SingleValueProvider<UnitNumber<Bandwidth>>("downloadBandwidth")
    val persistentStorageAvailable =
            SingleValueProvider<UnitNumber<AmountOfData>>("persistentStorageAvailable")

    override fun toString() = "DeviceProfile " +
            listOf(uploadBandwidth, downloadBandwidth, persistentStorageAvailable)
                    .joinMultiLine(indentation = 2)

    init { init() }
}
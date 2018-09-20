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

import org.junit.jupiter.api.Test
import straightway.units.bit
import straightway.units.byte
import straightway.units.div
import straightway.units.get
import straightway.units.gi
import straightway.units.me
import straightway.units.second

class DeviceProfileTest {

    private val sut get() = testProfile<DeviceProfile> { DeviceProfile(it) }

    @Test
    fun uploadBandwidth() = sut.testSingleValue(3[me(bit) / second]) { uploadBandwidth }

    @Test
    fun downloadBandwidth() = sut.testSingleValue(3[me(bit) / second]) { downloadBandwidth }

    @Test
    fun persistentStorageAvailable() = sut.testSingleValue(3[gi(byte)]) {
        persistentStorageAvailable
    }
}
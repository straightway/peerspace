/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.general.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BandwidthTest {
    @Test fun kbps_toString() = assertEquals("kbyte/s", ((kilo(byte) / second).toString()))
    @Test fun kbps_scaling() = assertEquals(1000[kilo(byte) / second], 1[mega(byte) / second])
    @Test fun conversion_kbps_to_kbyteps() = assertEquals(8.0, 1[kilo(byte) / second][kilo(bit) / second].value)
    @Test fun conversion_kbyteps_to_kbps() = assertEquals(1.0, 8[kilo(bit) / second][kilo(byte) / second].value)
    @Suppress("USELESS_IS_CHECK")
    @Test fun preDefinedQuantity() = assertTrue(kilo(byte) / second is Bandwidth)
}
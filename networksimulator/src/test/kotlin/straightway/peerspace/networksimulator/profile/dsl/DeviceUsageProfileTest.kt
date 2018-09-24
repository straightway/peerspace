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
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DeviceUsageProfileTest {

    private val sut get() = testProfile<DeviceUsageProfile> { DeviceUsageProfile(it) }

    @Test
    fun onlineTimes() = sut.testMultiValue { onlineTimes }

    @Test
    fun usages() = sut.testMultiValue { usages }

    @Test
    fun device() = sut.testSingleValue(DeviceProfile {}) { device }

    @Test
    fun `toString without set values`() =
            Given {
                DeviceUsageProfile { }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_
                               "DeviceUsageProfile {\n" +
                               "  onlineTimes = <unset>\n" +
                               "  usages = <unset>\n" +
                               "  device = <unset>\n" +
                               "}")
            }
}
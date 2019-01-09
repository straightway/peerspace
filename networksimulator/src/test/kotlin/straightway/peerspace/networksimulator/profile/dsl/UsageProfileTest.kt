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
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.byte
import straightway.units.get
import straightway.units.second

class UsageProfileTest {

    private val sut get() = testProfile<UsageProfile> { UsageProfile("description", it) }

    @Test
    fun description() = expect(sut {}.description is_ Equal to_ "description")

    @Test
    fun activity() = sut.testSingleValue(Activity("activityName") {}) { activity }

    @Test
    fun numberOfTimes() = sut.testSingleValue(3) { numberOfTimes }

    @Test
    fun duration() = sut.testSingleValue(3.0[second]) { duration }

    @Test
    fun time() = sut.testSingleValue(Weekly.mondays) { time }

    @Test
    fun dataVolume() = sut.testSingleValue(1[byte]) { dataVolume }

    @Test
    fun `update by invoke yields same instance`() =
            Given {
                UsageProfile("description") {}
            } when_ {
                this {}
            } then {
                expect(it.result is_ Same as_ this)
            }

    @Test
    fun `update by invoke alters instance`() =
            Given {
                UsageProfile("description") {}
            } when_ {
                this {
                    numberOfTimes { 3 }
                }
            } then {
                expect(numberOfTimes.value is_ Equal to_ 3)
            }

    @Test
    fun `toString without set values`() =
            Given {
                UsageProfile("description") { }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_
                               "UsageProfile(description) {\n" +
                               "  activity = <unset>\n" +
                               "  numberOfTimes = <unset>\n" +
                               "  duration = <unset>\n" +
                               "  time = <unset>\n" +
                               "  dataVolume = <unset>\n" +
                               "}")
            }
}
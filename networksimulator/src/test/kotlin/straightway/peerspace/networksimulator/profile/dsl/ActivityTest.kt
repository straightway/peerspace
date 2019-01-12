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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.networksimulator.user.ActivityEnvironment
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ActivityTest {

    @Test
    fun `name is accessible`() =
            Given {
                Activity("name") {}
            } when_ {
                name
            } then {
                expect(it.result is_ Equal to_ "name")
            }

    @Test
    fun `action is invoked with proper arguments`() {
        var calledEnvironment: ActivityEnvironment? = null
        val environment = ActivityEnvironment(
                mock(), mock(), mock(), UsageProfile("description") {})
        Given {
            Activity("name") { calledEnvironment = this }
        } when_ {
            this(environment)
        } then {
            expect(calledEnvironment is_ Same as_ environment)
        }
    }

    @Test
    fun `toString returns activity name`() =
            Given {
                Activity("name") {}
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name")
            }
}
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
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class StaticMultiValueTest {

    private val test get() =
        Given {
            StaticMultiValue<Any>("name")
        }

    @Test
    fun `name is accessible`() =
            test when_ {
                name
            } then {
                expect(it.result is_ Equal to_ "name")
            }

    @Test
    fun `invoke method sets values getter`() {
        var called = false
        test while_ {
            this { called = true; values() }
        } when_ {
            values
        } then {
            expect(called is_ True)
        }
    }

    @Test
    fun `values getter is directly called during invoke`() {
        var called = false
        test when_ {
            this { called = true; values() }
        } then {
            expect(called is_ True)
        }
    }

    @Test
    fun `values yields return values of getter`() =
            test while_ {
                this { values(1, 2, 3) }
            } when_ {
                values
            } then {
                expect(it.result is_ Equal to_ Values(1, 2, 3))
            }
}
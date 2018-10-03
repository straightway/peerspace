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
import straightway.error.Panic
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class SingleValueTest {

    private class Sut : SingleValue<String>("name") {
        override operator fun invoke(getter: SingleValue<String>.() -> String) {
            valueBackingField = getter()
        }

        public override var valueBackingField: String? = null
    }

    private val test get() = Given { Sut() }

    @Test
    fun `name is as specified`() =
            test when_ {
                name
            } then {
                expect(it.result is_ Equal to_ "name")
            }

    @Test
    fun `values panics if valueBackingField is null`() =
            test while_ {
                valueBackingField = null
            } when_ {
                value
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `value returns non-null valueBackingField`() =
            test while_ {
                valueBackingField = "hello"
            } when_ {
                value
            } then {
                expect(it.result is_ Equal to_ "hello")
            }

    @Test
    fun `toString for unset field`() =
            test when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = <unset>")
            }

    @Test
    fun `toString for set field`() =
            test while_ {
                valueBackingField = "hello"
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = hello")
            }
}
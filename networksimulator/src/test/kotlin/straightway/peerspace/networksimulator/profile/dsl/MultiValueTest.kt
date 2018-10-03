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

class MultiValueTest {

    private class Sut : MultiValue<String>("name") {
        override fun setValuesFrom(getter: () -> List<String>) {
            valuesBackingField = getter()
        }

        public override var valuesBackingField: Iterable<String>? = null
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
    fun `values panics if valuesBackingField is null`() =
            test while_ {
                valuesBackingField = null
            } when_ {
                values
            } then {
                expect({ it.result } does Throw.type<Panic>())
            }

    @Test
    fun `values returns value of valuesBackingField if not null`() =
            test while_ {
                valuesBackingField = listOf("1", "2", "3")
            } when_ {
                values
            } then {
                expect(it.result is_ Equal to_ listOf("1", "2", "3"))
            }

    @Test
    fun `invoke result is assigned to valuesBackingField`() =
            test when_ {
                this { values("1", "2", "3") }
            } then { _ ->
                expect(values is_ Equal to_ listOf("1", "2", "3"))
            }

    @Test
    fun `invoke block adds values with unary plus`() =
            test when_ {
                this { +"1"; +"2"; +"3" }
            } then { _ ->
                expect(values is_ Equal to_ listOf("1", "2", "3"))
            }

    @Test
    fun `second call of values function overrides first call`() =
            test when_ {
                this {
                    values("4", "5", "6")
                    values("1", "2", "3")
                }
            } then { _ ->
                expect(values is_ Equal to_ listOf("1", "2", "3"))
            }

    @Test
    fun `unary plus for a list of of values adds all values`() =
            test when_ {
                this {
                    values("1")
                    +listOf("2", "3")
                }
            } then { _ ->
                expect(values is_ Equal to_ listOf("1", "2", "3"))
            }

    @Test
    fun `toString for single line`() =
            test while_ {
                this { values("1", "2", "3") }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = [1, 2, 3]")
            }

    @Test
    fun `toString for unset value`() =
            test when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = <unset>")
            }

    @Test
    fun `toString for multi line`() =
            test while_ {
                this { values("1", "2\n2.1", "3") }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = [\n  1,\n  2\n  2.1,\n  3\n]")
            }
}
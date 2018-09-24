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
import straightway.testing.flow.False
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class MultiValueProviderTest {

    private val test get() =
        Given {
            MultiValueProvider<Any>("name")
        }

    @Test
    fun `name is accessible`() =
            test when_ {
                name
            } then {
                expect(it.result is_ Equal to_ "name")
            }

    @Test
    fun `access to not initialized values panics`() =
            test when_ {
                values
            } then {
                expect({ it.result } does Throw.type<Panic>())
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
    fun `values getter is not called during invoke`() {
        var called = false
        test when_ {
            this { called = true; values() }
        } then {
            expect(called is_ False)
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

    @Test
    fun `unary plus yields single value list`() =
            test while_ {
                this { +"Hello" }
            } when_ {
                values
            } then {
                expect(it.result is_ Equal to_ Values("Hello"))
            }

    @Test
    fun `binary plus adds new list element`() =
            test while_ {
                this { +"Hello" + "World" }
            } when_ {
                values
            } then {
                expect(it.result is_ Equal to_ Values("Hello", "World"))
            }

    @Test
    fun `toString for unset values`() =
            Given {
                MultiValueProvider<Int>("name")
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = <unset>")
            }

    @Test
    fun `toString for set values`() =
            Given {
                MultiValueProvider<Int>("name")
            } while_ {
                this { values(1, 2, 3, 4) }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "name = [1, 2, 3, 4]")
            }

    @Test
    fun `toString for set values with multiline string representation`() =
            Given {
                MultiValueProvider<String>("name")
            } while_ {
                this { values("single line", "multi\nline") }
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_
                               "name = [\n" +
                               "  single line,\n" +
                               "  multi\n" +
                               "  line\n" +
                               "]")
            }
}
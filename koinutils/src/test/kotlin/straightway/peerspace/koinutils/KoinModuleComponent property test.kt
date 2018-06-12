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
package straightway.peerspace.koinutils

import org.junit.jupiter.api.Test
import org.koin.dsl.context.Context
import straightway.expr.minus
import straightway.peerspace.koinutils.Bean.get
import straightway.peerspace.koinutils.Property.getProperty
import straightway.peerspace.koinutils.Property.releaseProperties
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.util.Date

class `KoinModuleComponent property test` : KoinTestBase() {

    private val testBinding get() = Given {
        object {
            private lateinit var module: IndependentModule
            val properties = mutableMapOf<String, Any>()

            val sut: KoinModuleComponent by lazy {
                WithModules(module)
                        .apply { extraProperties.putAll(properties) }
                        .make { KoinModuleComponent() }
            }

            fun declare(init: Context.() -> Unit) {
                module = independentContext {
                    init()
                }
            }

            fun addProperties(vararg additionalProperties: Pair<String, Any>) =
                    additionalProperties.forEach { properties[it.first] = it.second }
        }
    }

    @Test
    fun `getProperty yields property value`() =
            testBinding when_ {
                declare { koinContext.setProperty("Property.Key", "Hello") }
            } then {
                expect(sut.getProperty<String>("Property.Key") is_ Equal to_ "Hello")
            }

    @Test
    fun `getProperty with default value`() =
            testBinding when_ {
                declare {}
            } then {
                expect(sut.getProperty("Property.Key", "abc") is_ Equal to_ "abc")
            }

    @Test
    fun `getProperty with converter`() =
            testBinding when_ {
                declare { koinContext.setProperty("Property.Key", "A") }
            } then {
                expect(sut.getProperty("Property.Key") { it + it } is_ Equal to_ "AA")
            }

    @Test
    fun `getProperty with converter and default value`() =
            testBinding when_ {
                declare { }
            } then {
                expect(sut.getProperty("Property.Key", "B") { it + it } is_ Equal to_ "B")
            }

    @Test
    fun `releaseProperties removes properties from context`() =
            testBinding while_ {
                declare { }
                sut.context.setProperty("Hello", "World")
            } when_ {
                sut.releaseProperties("Hello")
            } then {
                expect(sut.getProperty("Hello", "Mars") is_ Equal to_ "Mars")
            }

    @Test
    fun `sub context works`() =
            testBinding while_ {
                declare {
                    context("ctx") {
                        bean { Date(12345L) }
                    }
                }
            } when_ {
                sut.get<Date>()
            } then {
                expect(it.result is_ Equal to_ Date(12345L))
            }

    @Test
    fun `releaseContext releases a sub context`() {
        lateinit var date: Date
        testBinding while_ {
            declare {
                context("ctx") { bean { Date(12345L) } }
            }
            date = sut.get()
            expect(sut.get<Date>() is_ Same as_ date)
        } when_ {
            sut.releaseContext("ctx")
        } then {
            expect(sut.get<Date>() is_ Equal to_ date)
            expect(sut.get<Date>() is_ Not - Same as_ date)
        }
    }

    @Test
    fun `addProperties Test`() =
            testBinding while_ {
                addProperties(
                    "A" to 1,
                    "B" to 2)
            } when_ {
                properties
            } then {
                expect(it.result is_ Equal to_ mapOf("A" to 1, "B" to 2))
            }
}
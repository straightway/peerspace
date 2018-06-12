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
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import javax.activity.InvalidActivityException

class `KoinModuleComponent binding Test` : KoinTestBase() {

    private class TestComponent(
            val constructorBound: String
    ) : KoinModuleComponent by KoinModuleComponent() {
        val injectionUnnamed by inject<String>()
        val injectionUnnamedWithParameters by inject<Double> { mapOf(Pair("p", 3.14)) }
        val injectionNamed by inject<Int>("bean")
        val injectionNamedWithParameters
                by inject<Int>("fun") { mapOf(Pair("p", 2)) }
        val propertyWithoutDefault by property<String>("Property.Key")
        val propertyWithDefault by property("Property.Key", "Default")
        val propertyWithConverter by property("Property.Key") { it + it }
        val propertyWithConverterAndDefault by property("Property.Key", "Default") { it + it }
    }

    private class OtherComponent : KoinModuleComponent by KoinModuleComponent() {
        val injectionUnnamed by inject<String>()
    }

    private val testBinding get() = Given {
        object {

            private lateinit var module: IndependentModule

            val sut: TestComponent by lazy {
                WithModules(module).make { TestComponent(get()) }
            }

            fun declare(init: Context.() -> Unit) {
                module = independentContext {
                    bean { "StringBean" }
                    init()
                }
            }
        }
    }

    @Test
    fun `static invoke method returns instance`() =
            Given {} when_ {
                WithModules().make { KoinModuleComponent() }
            } then {
                expect(it.result.context is_ Not - Null)
            }

    @Test
    fun `binding of constructor parameters work`() =
            testBinding when_ {
                declare { bean {"Hello World!"} }
            } then {
                expect(sut.constructorBound is_ Equal to_ "Hello World!")
            }

    @Test
    fun `binding of unnamed properties by injection works`() =
            testBinding when_ {
                declare { bean { "Hello World!" } }
            } then {
                expect(sut.injectionUnnamed is_ Equal to_ "Hello World!")
            }

    @Test
    fun `binding of named properties by injection works`() =
            testBinding when_ {
                declare { bean("bean") { 83 } }
            } then {
                expect(sut.injectionNamed is_ Equal to_ 83)
            }

    @Test
    fun `binding of named properties with parameters by injection works`() =
            testBinding when_ {
                declare { bean("fun") { it.get<Int>("p") } }
            } then {
                expect(sut.injectionNamedWithParameters is_ Equal to_ 2)
            }

    @Test
    fun `binding of unnamed properties with parameters by injection works`() =
            testBinding when_ {
                declare { bean { it.get<Double>("p") } }
            } then {
                expect(sut.injectionUnnamedWithParameters is_ Equal to_ 3.14)
            }

    @Test
    fun `property without default value`() =
            testBinding when_ {
                declare { koinContext.setProperty("Property.Key", "Hello") }
            } then {
                expect(sut.propertyWithoutDefault is_ Equal to_ "Hello")
            }

    @Test
    fun `property with converter`() =
            testBinding when_ {
                declare { koinContext.setProperty("Property.Key", "Hello") }
            } then {
                expect(sut.propertyWithConverter is_ Equal to_ "HelloHello")
            }

    @Test
    fun `property with default value`() =
            testBinding when_ {
                declare {  }
            } then {
                expect(sut.propertyWithDefault is_ Equal to_ "Default")
            }

    @Test
    fun `property with converter and default value`() =
            testBinding when_ {
                declare { }
            } then {
                expect(sut.propertyWithConverterAndDefault is_ Equal to_ "Default")
            }

    @Test
    fun `get without name and parameters returns bean by type`() =
            testBinding while_ {
                declare { bean { "Hello" } }
            } when_ {
                sut.get<String>()
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `get with name and without parameters returns bean by name`() =
            testBinding while_ {
                declare { bean("bean") { 83 } }
            } when_ {
                sut.get<Int>("bean")
            } then {
                expect(it.result is_ Equal to_ 83)
            }

    @Test
    fun `get with name and parameters returns bean by name`() =
            testBinding while_ {
                declare { bean("fun") { it.get<Int>("p") } }
            } when_ {
                sut.get<Int>("fun", { mapOf("p" to 2) })
            } then {
                expect(it.result is_ Equal to_ 2)
            }

    @Test
    fun `get without name and with parameters returns bean by type`() =
            testBinding while_ {
                declare { bean { it.get<Double>("p") } }
            } when_ {
                sut.get<Double> { mapOf("p" to 3.14) }
            } then {
                expect(it.result is_ Equal to_ 3.14)
            }

    @Test
    fun `withOwnContext sets context before action`() =
            testBinding while_ {
                declare { }
            } when_ {
                sut.withOwnContext { this }
            } then {
                expect(it.result is_ Same as_ sut.context)
            }

    @Test
    fun `withOwnContext resets context after action`() =
            testBinding while_ {
                declare { }
            } when_ {
                sut.withOwnContext { }
            } then {
                expect({ KoinModuleComponent.currentContext }
                               does Throw.type<ConstructedWithoutKoinModulesException>())
            }

    @Test
    fun `withOwnContext resets context even if action throws`() =
            testBinding while_ {
                declare { }
            } when_ {
                expect({ sut.withOwnContext { throw InvalidActivityException() } }
                               does Throw.type<InvalidActivityException>())
            } then {
                expect({ KoinModuleComponent.currentContext }
                               does Throw.type<ConstructedWithoutKoinModulesException>())
            }

    @Test
    fun `transitive unnamed references are properly resolved`() =
            testBinding while_ {
                declare {
                    bean { OtherComponent() }
                }
            } when_ {
                sut.get<OtherComponent>()
            } then {
                expect(it.result.injectionUnnamed is_ Equal to_ sut.injectionUnnamed)
            }

    @Test
    fun `transitive named references are properly resolved`() =
            testBinding while_ {
                declare {
                    bean("other") { OtherComponent() }
                }
            } when_ {
                sut.get<OtherComponent>("other")
            } then {
                expect(it.result.injectionUnnamed is_ Equal to_ sut.injectionUnnamed)
            }

    @Test
    fun `transitive unnamed references with parameters are properly resolved`() =
            testBinding while_ {
                declare {
                    bean { OtherComponent() }
                }
            } when_ {
                sut.get<OtherComponent> { mapOf("Hello" to "World") }
            } then {
                expect(it.result.injectionUnnamed is_ Equal to_ sut.injectionUnnamed)
            }

    @Test
    fun `transitive named references with parameters are properly resolved`() =
            testBinding while_ {
                declare {
                    bean("other") { OtherComponent() }
                }
            } when_ {
                sut.get<OtherComponent>("other") { mapOf("Hello" to "World") }
            } then {
                expect(it.result.injectionUnnamed is_ Equal to_ sut.injectionUnnamed)
            }
}
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
import org.koin.KoinContext
import straightway.expr.minus
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.Values
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class WithModulesTest : KoinTestBase() {

    @Test
    fun `isUsingEnvironmentProperties is initially false`() =
            Given {
                WithModules()
            } when_ {
                isUsingEnvironmentProperties
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `isUsingEnvironmentProperties can be set`() =
            Given {
                WithModules()
            } when_ {
                isUsingEnvironmentProperties = true
            } then {
                expect(isUsingEnvironmentProperties is_ True)
            }

    @Test
    fun `isUsingKoinPropertiesFile is initially true`() =
            Given {
                WithModules()
            } when_ {
                propertiesFile
            } then {
                expect(it.result is_ Equal to_ "/koin.properties")
            }

    @Test
    fun `isUsingKoinPropertiesFile can be set`() =
            Given {
                WithModules()
            } when_ {
                propertiesFile = null
            } then {
                expect(propertiesFile is_ Null)
            }

    @Test
    fun `extraProperties is initially empty`() =
            Given {
                WithModules()
            } when_ {
                extraProperties
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `extraProperties can be updated`() =
            Given {
                WithModules()
            } when_ {
                extraProperties["A"] = 1
            } then {
                expect(extraProperties is_ Equal to_ Values("A" to 1))
            }

    @Test
    fun `make uses specified modules`() =
            Given {
                WithModules(independentContext { bean { "Hello" } })
            } when_ {
                make { get<String>() }
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `withContext {} is a shorthand for WithModules(independentContext {})`() =
            Given {
                withContext { bean { "Hello" } }
            } when_ {
                make { get<String>() }
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `WithModules(vararg Context) is a shorthand for WithModules(List of Context)`() =
            Given {
                WithModules(listOf(independentContext { bean { "Hello" } }))
            } when_ {
                make { get<String>() }
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `properties are loaded from specified file`() =
            Given {
                withContext {}.apply { propertiesFile = "/test.properties" }
            } when_ {
                make { getProperty<String>("testProperty") }
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `extra properties are considered`() =
            Given {
                withContext {}.apply { extraProperties["testProperty"] = "Hello" }
            } when_ {
                make { getProperty<String>("testProperty") }
            } then {
                expect(it.result is_ Equal to_ "Hello")
            }

    @Test
    fun `environment properties are considered`() {
        val environmentVariables = System.getenv()
        val testKey = environmentVariables.keys.first()
        val testValue = environmentVariables[testKey]!!
        Given {
            withContext {}.apply { isUsingEnvironmentProperties = true }
        } when_ {
            make { getProperty<String>(testKey) }
        } then {
            expect(it.result is_ Equal to_ testValue)
        }
    }

    @Test
    fun `old context is restored afterwards`() {
        var threadSpecificContext: KoinContext? = null
        withContext {
            threadSpecificContext = KoinModuleComponent.currentContext
            withContext {
                expect(KoinModuleComponent.currentContext is_ Not - Same as_ threadSpecificContext!!)
            } make {
                expect(KoinModuleComponent.currentContext is_ Not - Same as_ threadSpecificContext!!)
            }
            expect(KoinModuleComponent.currentContext is_ Same as_ threadSpecificContext!!)
        } make {
            expect(KoinModuleComponent.currentContext is_ Same as_ threadSpecificContext!!)
        }

        expect(KoinModuleComponent.hasContext is_ False)
    }
}
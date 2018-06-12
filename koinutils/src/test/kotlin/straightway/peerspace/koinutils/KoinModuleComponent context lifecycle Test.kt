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
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.util.concurrent.Semaphore

class `KoinModuleComponent context lifecycle Test` : KoinTestBase() {

    private val test get() = Given {}

    @Test
    fun `initializing outside withModules throws`() =
            test when_ {
                KoinModuleComponent()
            } then {
                expect({ it.result } does
                               Throw.type<ConstructedWithoutKoinModulesException>())
            }

    @Test
    fun `initializer is called during initialization`() {
        var wasCalled = false
        WithModules().make {
            wasCalled = true
        }

        expect(wasCalled is_ True)
    }

    @Test
    fun `withModules returns result of initializer`() =
        test when_ { WithModules().make { 83 } } then {
            expect(it.result is_ Equal to_ 83)
        }

    @Test
    fun `context is set during initialization`() {
        WithModules().make {
            val sut = KoinModuleComponent()
            expect(sut.context is_ Not - Null)
        }
    }

    @Test
    fun `context kept after initialization`() {
        var sut: KoinModuleComponent? = null
        WithModules().make { sut = KoinModuleComponent() }
        expect(sut!!.context is_ Not - Null)
    }

    @Test
    fun `two separate initializations have two separate contexts`() {
        var context1: KoinContext? = null
        var context2: KoinContext? = null
        WithModules().make { context1 = KoinModuleComponent().context }
        WithModules().make { context2 = KoinModuleComponent().context }
        expect(context1!! is_ Not - Same as_ context2!!)
    }

    @Test
    fun `second initialization without context throws`() {
        WithModules().make { KoinModuleComponent() }
        expect({ KoinModuleComponent() } does
                       Throw.type<ConstructedWithoutKoinModulesException>())
    }

    @Test
    fun `parallel initialization in other thread without context throws`() {
        val enter = Semaphore(0)
        val finish = Semaphore(0)
        var isThreadEntered = false
        Thread {
            WithModules().make {
                isThreadEntered = true
                enter.release()
                finish.acquire()
            }
        }.start()

        enter.acquire()
        try {
            expect({ KoinModuleComponent() } does
                           Throw.type<ConstructedWithoutKoinModulesException>())
        } finally {
            finish.release()
        }

        expect(isThreadEntered is_ True)
    }

    @Test
    fun `specified modules are provided within withModules`() {
        var module = independentContext {
            bean("testBean") { "Hello World!" }
        }

        WithModules(module).make {
            val sut = KoinModuleComponent()
            val testBean = sut.context.get<String>("testBean")
            expect(testBean is_ Equal to_ "Hello World!")
        }
    }
}
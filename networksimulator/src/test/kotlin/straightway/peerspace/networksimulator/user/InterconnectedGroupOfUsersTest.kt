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
package straightway.peerspace.networksimulator.user

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.KoinModuleComponent
import straightway.testing.bdd.Given
import straightway.koinutils.withContext
import straightway.peerspace.networksimulator.profile.officeWorker
import straightway.random.Chooser
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.testing.flow.values

class InterconnectedGroupOfUsersTest : KoinLoggingDisabler() {

    private companion object {
        fun createUser() = withContext {
            bean("knownUsers") { mutableListOf<User>() }
            bean { officeWorker }
            factory { mock<Device>() }
        } make { User() }
    }

    @Test
    fun `unified group contains member of both groups`() =
            Given {
                createSutContexts(2)
            } when_ {
                get(0).sut.unifyWith(get(1).sut)
            } then {
                expect(it.result.members has flatMap { it.users }.values())
            }

    @Test
    fun `unified group members know each other`() =
            Given {
                createSutContexts(2)
            } when_ {
                get(0).sut.unifyWith(get(1).sut)
            } then {
                expect(get(0).users.single().knownUsers.contains(get(1).users.single()))
                expect(get(1).users.single().knownUsers.contains(get(0).users.single()))
            }

    @Test
    fun `unified group members are chosen with a chooser`() =
            Given {
                createSutContexts(2, 2)
            } while_ {
                forEach { it.chosenItem = 1 }
            } when_ {
                get(0).sut.unifyWith(get(1).sut)
            } then {
                expect(get(0).users[0].knownUsers is_ Empty)
                expect(get(0).users[1].knownUsers is_ Equal to_ Values(get(1).users[1]))
                expect(get(1).users[0].knownUsers is_ Empty)
                expect(get(1).users[1].knownUsers is_ Equal to_ Values(get(0).users[1]))
            }

    @Test
    fun `unifying with an empty group does not introduce users`() =
            Given {
                listOf(SutContext(1), SutContext(0))
            } when_ {
                get(0).sut.unifyWith(get(1).sut)
            } then {
                expect(get(0).users.single().knownUsers is_ Empty)
            }

    @Test
    fun `unifying an empty group does not introduce users`() =
            Given {
                listOf(SutContext(0), SutContext(1))
            } when_ {
                get(0).sut.unifyWith(get(1).sut)
            } then {
                expect(get(1).users.single().knownUsers is_ Empty)
            }

    private val KoinModuleComponent.sut get() =
        get<InterconnectedGroupOfUsers>()
    private val KoinModuleComponent.users get() =
        get<List<User>>("users")
    private val KoinModuleComponent.knownUsers get() =
        get<MutableList<User>>("knownUsers")
    private val KoinModuleComponent.sutContext get() =
        get<SutContext>()

    private fun createSutContexts(count: Int, numberOfUsers: Int = 1) =
            (1..count).map { SutContext(numberOfUsers) }

    private class SutContext(numbeOfUsers: Int) {
        var chosenItem = 0
        val chooser: Chooser = mock {
            on { chooseFrom<User>(any(), eq(1)) }.thenAnswer {
                listOf((it.arguments[0] as List<*>)[chosenItem] as User)
            }
        }
        val users = (1..numbeOfUsers).map { createUser() }
        val sut = InterconnectedGroupOfUsers(chooser, users)
    }
}
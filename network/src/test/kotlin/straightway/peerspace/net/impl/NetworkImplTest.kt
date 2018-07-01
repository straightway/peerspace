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
package straightway.peerspace.net.impl

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QuerySource
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.Values
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class NetworkImplTest : KoinLoggingDisabler() {

    private companion object {
        val receiverId = Id("receiver")
    }

    private val test get() =
            Given {
                object {
                    var createdIds = listOf<Id>()
                    val pushTarget = mock<PushTarget>()
                    val querySource = mock<QuerySource>()
                    val environment = PeerTestEnvironment(
                            networkFactory = { NetworkImpl() }
                    ) {
                        factory {
                            createdIds += it.get<Id>("id")
                            pushTarget
                        }
                        factory {
                            createdIds += it.get<Id>("id")
                            querySource
                        }
                    }
                    val sut = environment.get<Network>() as NetworkImpl
                }
            }

    @Test
    fun `getPushTarget creates new instance via Koin`() =
            test when_ {
                sut.getPushTarget(receiverId)
            } then {
                expect(it.result is_ Same as_ pushTarget)
                expect(createdIds is_ Equal to_ Values(receiverId))
            }

    @Test
    fun `getQuerySource creates new instance via Koin`() =
            test when_ {
                sut.getQuerySource(receiverId)
            } then {
                expect(it.result is_ Same as_ querySource)
                expect(createdIds is_ Equal to_ Values(receiverId))
            }
}
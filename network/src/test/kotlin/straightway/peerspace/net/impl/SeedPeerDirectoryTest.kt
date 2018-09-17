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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.peerDirectory
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class SeedPeerDirectoryTest : KoinLoggingDisabler() {

    private companion object {
        val seedPeerIds = setOf(Id("seedPeerId1"), Id("seedPeerId2"))
        val wrappedPeerIds = setOf(Id("wrappedPeerId1"), Id("wrappedPeerId2"))
    }

    @Suppress("OptionalUnit")
    private val test get() =
        Given {
            object {
                var peerIds: Set<Id> = setOf()
                var unreachable: Set<Id> = setOf()
                val wrapped: PeerDirectory = mock { _ ->
                    on { allKnownPeersIds }.thenAnswer {
                        peerIds - unreachable
                    }
                    on { add(any()) }.thenAnswer {
                        peerIds += it.getArgument<Id>(0); Unit
                    }
                    on { setUnreachable(any()) }.thenAnswer {
                        unreachable += it.getArgument<Id>(0); Unit
                    }
                }
                val environment = PeerTestEnvironment(
                        configurationFactory = {
                            Configuration(seedPeerIds = seedPeerIds)
                        },
                        peerDirectoryFactory = {
                            SeedPeerDirectory(wrapped)
                        }
                )
                val sut = environment.peerDirectory
            }
        }

    @Test
    fun `wrapped directory empty yields seed peer ids`() =
            test when_ {
                sut.allKnownPeersIds
            } then {
                expect(it.result is_ Equal to_ seedPeerIds)
            }

    @Test
    fun `wrapped directory yields wrapped peer ids if not empty`() =
            test while_ {
                peerIds = wrappedPeerIds
            } when_ {
                sut.allKnownPeersIds
            } then {
                expect(it.result is_ Equal to_ wrappedPeerIds)
            }

    @Test
    fun `add new id to wrapped instance`() =
            test when_ {
                sut.add(wrappedPeerIds.first())
            } then {
                verify(wrapped).add(wrappedPeerIds.first())
            }

    @Test
    fun `adding seed id is ignored`() =
            test when_ {
                sut.add(seedPeerIds.first())
            } then {
                verify(wrapped, never()).add(any())
            }

    @Test
    fun `setUnreachable id to wrapped instance`() =
            test when_ {
                sut.setUnreachable(wrappedPeerIds.first())
            } then {
                verify(wrapped).setUnreachable(wrappedPeerIds.first())
            }

    @Test
    fun `setUnreachable seed id is ignored`() =
            test when_ {
                sut.setUnreachable(seedPeerIds.first())
            } then {
                verify(wrapped, never()).setUnreachable(any())
            }
}
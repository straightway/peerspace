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
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect

class ForwardStrategyImplTest {

    private val test get() = Given {
        object {
            val hashes = mutableListOf<Long>()
            val hasher = mock<KeyHasher> {
                on { getHashes(any()) }.thenAnswer { hashes }
            }
            val sut = ForwardStrategyImpl(hasher)
        }
    }

    @Test
    fun `getPushForwardPeerIdsFor is not implemented`() =
            test when_ {
                sut.getPushForwardPeerIdsFor(Key(Id("chunkId")), ForwardState())
            } then {
                expect({ it.result } does Throw.type<NotImplementedError>())
            }

    @Test
    fun `getQueryForwardPeerIdsFor is not implemented`() =
            test when_ {
                sut.getQueryForwardPeerIdsFor(
                        QueryRequest(Id("originatorId"), Id("chunkId")),
                        ForwardState())
            } then {
                expect({ it.result } does Throw.type<NotImplementedError>())
            }
}
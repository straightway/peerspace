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

import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.chunkId
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.idForHash
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class `ForwardStrategyImplTest getForwardPeerIdsFor QueryRequest` : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originatorId")
        val queryRequest = DataQueryRequest(originatorId, DataQuery(chunkId))
    }

    private val test get() = Given {
        ForwardStrategyTestEnvironment().apply {
            hashes[queryRequest] = listOf(100L)
            hashes[Key(chunkId)] = listOf(100L)
        }
    }

    @Test
    fun `fresh query is forwarded to single known nearer peer`() =
            test while_ {
                addKnownPeerForHash(100)
            } when_ {
                sut.getForwardPeerIdsFor(queryRequest, ForwardState())
            } then {
                expect(it.result is_ Equal to_ Values(idForHash[100]!!))
            }
}
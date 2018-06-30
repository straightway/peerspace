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
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class QueryForwarderTest : KoinLoggingDisabler() {

    private companion object {
        val queryRequest = QueryRequest(Id("originatorId"), Id("chunkId"))
    }

    private val test get() =
        Given {
            object {
                var forwardPeerIds = listOf<Id>()
                val environment = PeerTestEnvironment(
                    knownPeersIds = ids("targetId"),
                    queryForwarderFactory = { QueryForwarder() },
                    forwardStrategyFactory = {
                        mock {
                            on { getQueryForwardPeerIdsFor(any(), any()) }.thenAnswer {
                                forwardPeerIds
                            }
                        }
                    })
                val sut get() =
                    environment.get<Forwarder<QueryRequest, QueryRequest>>("queryForwarder")
                val forwardStrategy get() =
                    environment.get<ForwardStrategy>()
            }
        }

    @Test
    fun `getKeyFor returns query request also as key`() =
            test when_ {
                sut.getKeyFor(queryRequest)
            } then {
                expect(it.result is_ Equal to_ queryRequest)
            }

    @Test
    fun `getForwardPeerIdsFor returns query forward peer ids from strategy` () =
            test while_ {
                forwardPeerIds = listOf(environment.knownPeersIds.first())
            } when_ {
                sut.getForwardPeerIdsFor(queryRequest, ForwardState())
            } then {
                expect(it.result is_ Equal to_ forwardPeerIds)
            }

    @Test
    fun `getForwardPeerIdsFor passes forward state to forward strategy` () {
        val forwardState = ForwardState(pending = setOf(Id("pendingId")))
        test when_ {
            sut.getForwardPeerIdsFor(queryRequest, forwardState)
        } then {
            verify(forwardStrategy).getQueryForwardPeerIdsFor(queryRequest, forwardState)
        }
    }

    @Test
    fun `forwardTo pushes to specified peer`() {
        val resultListener = object : TransmissionResultListener {
            override fun notifySuccess() { Assertions.fail<Panic>("Must not be called") }
            override fun notifyFailure() { Assertions.fail<Panic>("Must not be called") }
        }

        test when_ {
            sut.forwardTo(environment.knownPeersIds[0], queryRequest, resultListener)
        } then {
            val forwardedQuery = queryRequest.copy(originatorId = environment.peerId)
            verify(environment.knownPeers[0]).query(forwardedQuery, resultListener)
        }
    }
}
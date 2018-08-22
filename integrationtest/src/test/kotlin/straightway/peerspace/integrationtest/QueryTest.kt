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
package straightway.peerspace.integrationtest

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Peer
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.testing.bdd.Given

class QueryTest : KoinLoggingDisabler() {
    private val test get() =
            Given {
                object {
                    var queryForwardPeerIds = setOf<Id>()
                    var pushForwardPeerIds = setOf<Id>()
                    val environment = SinglePeerEnvironment(
                        forwardStrategyFactory =
                        {
                            mock { _ ->
                                on { getForwardPeerIdsFor(any(), any()) }.thenAnswer {
                                    when (it.arguments[0]) {
                                        is DataQueryRequest -> queryForwardPeerIds
                                        else -> pushForwardPeerIds
                                    }
                                }
                            }
                        })
                }
            }

    @Test
    fun `chunk is not forwarded twice due to query and forward strategy`() {
        val queryer = mock<Peer> {
            on { id }.thenReturn(Id("queryerId"))
        }

        val pusher = mock<Peer> {
            on { id }.thenReturn(Id("pusherId"))
        }

        val chunk = DataChunk(Key(Id("ChunkId")), byteArrayOf())

        test while_ {
            environment.addRemotePeer(queryer)
            environment.addRemotePeer(pusher)
            pushForwardPeerIds = setOf(queryer.id)
        } when_ {
            environment.peer.query(DataQueryRequest(queryer.id, DataQuery(chunk.key.id)))
            environment.peer.push(DataPushRequest(pusher.id, chunk))
            environment.simulator.run()
        } then {
            verify(queryer, times(1))
                    .push(eq(DataPushRequest(environment.peer.id, chunk)))
        }
    }
}
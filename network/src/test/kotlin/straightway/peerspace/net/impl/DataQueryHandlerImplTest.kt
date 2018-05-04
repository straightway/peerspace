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
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.Values
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataQueryHandlerImplTest {

    private val test get() =
            Given {
                object {
                    val push = PushRequest(
                            Id("originator"),
                            Chunk(Key(Id("chunkId")), byteArrayOf()))
                    val untimedId = Id("untimed")
                    val timedId = Id("timed")
                    val timedUntimedId = Id("timedUntimed")
                    val untimedDataQueryHandler = mock<DataQueryHandler> {
                        on { getForwardPeerIdsFor(any()) }
                                .thenReturn(listOf(untimedId, timedUntimedId))
                    }
                    val timedDataQueryHandler = mock<DataQueryHandler> {
                        on { getForwardPeerIdsFor(any()) }
                                .thenReturn(listOf(timedId, timedUntimedId))
                    }
                    val infrastructure = mock<Infrastructure>()
                    val untimedQuery = QueryRequest(Id("originator"), Id("untimedQuery"))
                    val timedQuery = QueryRequest(Id("originator"), Id("timedQuery"), 1L..2L)
                    val sut = DataQueryHandlerImpl(
                            untimedDataQueryHandler,
                            timedDataQueryHandler)
                }
            }

    @Test
    fun `getting infrastructure throws`() =
            test when_ { sut.infrastructure } then {
                expect({ it.result } does Throw.type<UnsupportedOperationException>())
            }

    @Test
    fun `setting infrastructure forwards to timed subhandler`() =
            test when_ { sut.infrastructure = infrastructure } then {
                verify(timedDataQueryHandler).infrastructure = infrastructure
            }

    @Test
    fun `setting infrastructure forwards to untimed subhandler`() =
            test when_ { sut.infrastructure = infrastructure } then {
                verify(untimedDataQueryHandler).infrastructure = infrastructure
            }

    @Test
    fun `handle forwards untimed query to untimed handler`() =
            test when_ { sut.handle(untimedQuery) } then {
                verify(untimedDataQueryHandler).handle(untimedQuery)
            }

    @Test
    fun `handle does not forward untimed query to timed handler`() =
            test when_ { sut.handle(untimedQuery) } then {
                verify(timedDataQueryHandler, never()).handle(any())
            }

    @Test
    fun `handle forwards timed query to timed handler`() =
            test when_ { sut.handle(timedQuery) } then {
                verify(timedDataQueryHandler).handle(timedQuery)
            }

    @Test
    fun `handle does not forward timed query to untimed handler`() =
            test when_ { sut.handle(timedQuery) } then {
                verify(untimedDataQueryHandler, never()).handle(any())
            }

    @Test
    fun `getForwardPeerIdsFor yields ids returned by untimed subhandler`() =
            test when_ { sut.getForwardPeerIdsFor(push) } then {
                verify(untimedDataQueryHandler).getForwardPeerIdsFor(push)
                expect(it.result has Values(untimedId, timedUntimedId))
            }

    @Test
    fun `getForwardPeerIdsFor yields ids returned by timed subhandler`() =
            test when_ { sut.getForwardPeerIdsFor(push) } then {
                verify(timedDataQueryHandler).getForwardPeerIdsFor(push)
                expect(it.result has Values(timedId, timedUntimedId))
            }

    @Test
    fun `getForwardPeerIdsFor does not yield duplicates`() =
            test when_ { sut.getForwardPeerIdsFor(push) } then {
                expect(it.result.size is_ Equal to_ 3)
            }
}
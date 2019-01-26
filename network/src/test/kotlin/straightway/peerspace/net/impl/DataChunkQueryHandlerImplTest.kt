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
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.withContext
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataChunkQueryHandlerImplTest : KoinLoggingDisabler() {

    private companion object {
        val chunkKey = Key(Id("chunkId"))
    }

    private val test get() =
            Given {
                object {
                    val push = Request(Id("originator"), DataChunk(chunkKey, byteArrayOf()))
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
                    val untimedQuery = Request(
                            Id("originator"), DataChunkQuery(Id("untimedQuery")))
                    val timedQuery = Request(
                            Id("originator"), DataChunkQuery(Id("timedQuery"), 1L..2L))
                    val sut = withContext {
                        bean("untimedDataQueryHandler") { untimedDataQueryHandler }
                        bean("timedDataQueryHandler") { timedDataQueryHandler }
                    } make {
                        DataQueryHandlerImpl()
                    }
                }
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
            test when_ { sut.getForwardPeerIdsFor(push.content.key) } then {
                verify(untimedDataQueryHandler).getForwardPeerIdsFor(push.content.key)
                expect(it.result has Values(untimedId, timedUntimedId))
            }

    @Test
    fun `getForwardPeerIdsFor yields ids returned by timed subhandler`() =
            test when_ { sut.getForwardPeerIdsFor(push.content.key) } then {
                verify(timedDataQueryHandler).getForwardPeerIdsFor(push.content.key)
                expect(it.result has Values(timedId, timedUntimedId))
            }

    @Test
    fun `getForwardPeerIdsFor does not yield duplicates`() =
            test when_ { sut.getForwardPeerIdsFor(push.content.key) } then {
                expect(it.result.size is_ Equal to_ 3)
            }

    @Test
    fun `notifyChunkForwarded forwards timedKey only to timed handler`() {
        val key = Key(Id("0815"), 1L, 0)
        test when_ { sut.notifyChunkForwarded(key) } then {
            verify(timedDataQueryHandler).notifyChunkForwarded(key)
            verify(untimedDataQueryHandler, never()).notifyChunkForwarded(any())
        }
    }

    @Test
    fun `notifyChunkForwarded forwards untimedKey only to untimed handler`() {
        val key = Key(Id("0815"))
        test when_ { sut.notifyChunkForwarded(key) } then {
            verify(untimedDataQueryHandler).notifyChunkForwarded(key)
            verify(timedDataQueryHandler, never()).notifyChunkForwarded(any())
        }
    }
}
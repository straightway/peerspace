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
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.calls
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import straightway.expr.minus
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.configuration
import straightway.peerspace.net.dataPushTarget
import straightway.peerspace.net.dataQuerySource
import straightway.peerspace.net.localDeliveryEvent
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.toDuration
import java.time.LocalDateTime

class PeerClientImplTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunk")
        val timedQuery = DataQuery(chunkId, 1L..2L)
        val untimedQuery = DataQuery(chunkId)
        val timedMatchingChunk = DataChunk(Key(chunkId, 1L), byteArrayOf())
        val untimedMatchingChunk = DataChunk(Key(chunkId), byteArrayOf())
        val notMatchingChunk = DataChunk(Key(Id("otherChunkId")), byteArrayOf())
    }

    private val test get() =
        Given {
            object {
                var now = LocalDateTime.of(0, 1, 1, 0, 0)
                val environment = PeerTestEnvironment(
                        timeProviderFactory = {
                            mock { _ ->
                                on { now }.thenAnswer { now }
                            }
                        }
                ) {
                    bean { PeerClientImpl() }
                }

                val sut get() = environment.get<PeerClientImpl>()
            }
        }

    @Test
    fun `query sends query to data query source`() =
        test when_ {
            sut.query(untimedQuery) { fail { "do not call" } }
        } then {
            verify(environment.dataQuerySource).queryData(any())
        }

    @Test
    fun `query passes peerId as originatorId to DataQuerySource`() =
        test when_ {
            sut.query(untimedQuery) { fail { "do not call" } }
        } then {
            verify(environment.dataQuerySource).queryData(argThat {
                remotePeerId == environment.peerId
            })
        }

    @Test
    fun `query uses the given query to DataQuerySource`() =
        test when_ {
            sut.query(untimedQuery) { fail { "do not call" } }
        } then {
            verify(environment.dataQuerySource).queryData(argThat {
                content == untimedQuery
            })
        }

    @Test
    fun `query callback is notified when matching chunk arrives`() {
        var result: DataChunk? = null
        test while_ {
            sut.query(untimedQuery) { result = it }
        } when_ {
            (environment.localDeliveryEvent)(untimedMatchingChunk)
        } then {
            expect(result is_ Equal to_ untimedMatchingChunk)
        }
    }

    @Test
    fun `query callback is not notified when not matching chunk arrives`() =
        test while_ {
            sut.query(untimedQuery) { fail { "do not call" } }
        } when_ {
            (environment.localDeliveryEvent)(notMatchingChunk)
        } then {
            expect({ it.result } does Not - Throw.exception)
        }

    @Test
    fun `timed query callback is not notified any more when receiving is stopped`() {
        var result: DataChunk? = null
        test while_ {
            sut.query(timedQuery) { result = it; stopReceiving() }
        } when_ {
            (environment.localDeliveryEvent)(timedMatchingChunk)
            result = null
            (environment.localDeliveryEvent)(timedMatchingChunk)
        } then {
            expect(result is_ Null)
        }
    }

    @Test
    fun `timed query callback is not pending any more when receiving is stopped`() {
        test while_ {
            sut.query(timedQuery) { stopReceiving() }
        } when_ {
            (environment.localDeliveryEvent)(timedMatchingChunk)
        } then {
            expect(sut.numberOfPendingQueries is_ Equal to_ 0)
        }
    }

    @Test
    fun `untimed query callback is not notified when matching chunk arrives again`() {
        var result: DataChunk? = null
        test while_ {
            sut.query(untimedQuery) { result = it }
        } when_ {
            (environment.localDeliveryEvent)(untimedMatchingChunk)
            result = null
            (environment.localDeliveryEvent)(untimedMatchingChunk)
        } then {
            expect(result is_ Null)
        }
    }

    @Test
    fun `timed query callback is notified when matching chunk arrives again`() {
        var result: DataChunk? = null
        test while_ {
            sut.query(timedQuery) { result = it }
        } when_ {
            (environment.localDeliveryEvent)(timedMatchingChunk)
            result = null
            (environment.localDeliveryEvent)(timedMatchingChunk)
        } then {
            expect(result is_ Equal to_ timedMatchingChunk)
        }
    }

    @Test
    fun `timed query callback is not notified after timeout`() =
            test while_ {
                sut.query(timedQuery) { fail { "do not call" } }
                now += environment.configuration.timedDataQueryTimeout.toDuration()
            } when_ {
                (environment.localDeliveryEvent)(timedMatchingChunk)
            } then {
                expect({ it.result } does Not - Throw.exception)
            }

    @Test
    fun `timed query is not pending after timeout when chunk arrives`() =
            test while_ {
                sut.query(timedQuery) { fail { "do not call" } }
                now += environment.configuration.timedDataQueryTimeout.toDuration()
            } when_ {
                (environment.localDeliveryEvent)(notMatchingChunk)
            } then {
                expect(sut.numberOfPendingQueries is_ Equal to_ 0)
            }

    @Test
    fun `timed query is not pending after timeout when other query is issued`() =
            test while_ {
                sut.query(timedQuery) { fail { "do not call" } }
                now += environment.configuration.timedDataQueryTimeout.toDuration()
            } when_ {
                sut.query(untimedQuery) { fail { "do not call" } }
            } then {
                expect(sut.numberOfPendingQueries is_ Equal to_ 1)
            }

    @Test
    fun `timed query is not pending after timeout when store is issued`() =
            test while_ {
                sut.query(timedQuery) { fail { "do not call" } }
                now += environment.configuration.timedDataQueryTimeout.toDuration()
            } when_ {
                sut.store(notMatchingChunk)
            } then {
                expect(sut.numberOfPendingQueries is_ Equal to_ 0)
            }

    @Test
    fun `timed query callback is not pending after stopping from outside`() =
            test when_ {
                sut.query(timedQuery) { fail { "do not call" } }.stopReceiving()
            } then {
                expect(sut.numberOfPendingQueries is_ Equal to_ 0)
            }

    @Test
    fun `query is passed to expiration callback`() =
        test while_ {
            sut.query(timedQuery) { fail { "do not call" } }.onExpiring {
                expect(it is_ Equal to_ timedQuery)
            }
            now += environment.configuration.timedDataQueryTimeout.toDuration()
        } when_ {
            sut.store(notMatchingChunk)
        } then {
            expect({ it.result } does Not - Throw.exception)
        }

    @Test
    fun `expiration callbacks are called when pending query is removed`() {
        var expirationCalls = 0
        test while_ {
            val control = sut.query(timedQuery) { fail { "do not call" } }
            control.onExpiring {
                ++expirationCalls
            }
            control.onExpiring {
                ++expirationCalls
            }
            now += environment.configuration.timedDataQueryTimeout.toDuration()
        } when_ {
            sut.store(notMatchingChunk)
        } then {
            expect(expirationCalls is_ Equal to_ 2)
        }
    }

    @Test
    fun `timed query can be kept alive in expiration callback`() =
            test while_ {
                sut.query(timedQuery) { fail { "do not call" } }.onExpiring { keepAlive() }
                now += environment.configuration.timedDataQueryTimeout.toDuration()
            } when_ {
                sut.store(notMatchingChunk)
            } then {
                expect(sut.numberOfPendingQueries is_ Equal to_ 1)
            }

    @Test
    fun `query is not yet expired in expiration callback`() =
        test while_ {
            sut.query(timedQuery) { fail { "do not call" } }.onExpiring {
                expect(sut.numberOfPendingQueries is_ Equal to_ 1)
            }
            now += environment.configuration.timedDataQueryTimeout.toDuration()
        } when_ {
            sut.store(notMatchingChunk)
        } then {
            expect({ it.result } does Not - Throw.exception)
        }

    @Test
    fun `keepAlive re-issues query`() =
            test when_ {
                sut.query(timedQuery) { fail { "do not call" } }.keepAlive()
            } then {
                inOrder(environment.dataQuerySource) {
                    verify(environment.dataQuerySource, calls(2)).queryData(any())
                }
            }

    @Test
    fun `store pushes to local peer`() =
            test when_ {
                sut.store(untimedMatchingChunk)
            } then {
                verify(environment.dataPushTarget).pushDataChunk(argThat {
                    content == untimedMatchingChunk
                })
            }

    @Test
    fun `store pushes with local peer id as originator`() =
            test when_ {
                sut.store(untimedMatchingChunk)
            } then {
                verify(environment.dataPushTarget).pushDataChunk(argThat {
                    remotePeerId == environment.peerId
                })
            }
}
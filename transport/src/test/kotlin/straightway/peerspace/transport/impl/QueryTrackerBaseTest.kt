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
package straightway.peerspace.transport.impl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.Bean.get
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Size
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.testing.flow.of
import straightway.testing.flow.to_

class QueryTrackerBaseTest : KoinLoggingDisabler() {

    private class TestQueryTracker : QueryTrackerBase() {
        val receivedDataItems = mutableListOf<ByteArray>()
        var incompleteCalls = 0
        var timeoutCalls = 0
        var onIncompleteCallback: () -> Unit = {}
        var onTimeoutCallback: () -> Unit = {}
        override fun onReceived(data: ByteArray) { receivedDataItems.add(data) }
        override fun onIncomplete() { incompleteCalls++; onIncompleteCallback() }
        override fun onTimeout() { timeoutCalls++; onTimeoutCallback() }
        val protectedTransportQueryControl get() = transportQueryControl
        val protectedReceivedChunks get() = receivedChunks
        fun protectedQuery(chunkId: Id) = query(chunkId)
        fun protectedReceived(chunk: DataChunk, keepAlive: () -> Unit) = received(chunk, keepAlive)
    }

    private val test get() =
        Given {
            object : TransportTestEnvironment(
                    additionalInitialization = { bean { TestQueryTracker() } }
            ) {
                val sut get() = context.get<TestQueryTracker>()
            }
        }

    @Test
    fun `query is forwarded to peer client`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
            } then {
                verify(peerClient).query(eq(DataChunkQuery(Id("Id"))), any())
            }

    @Test
    fun `query calls onReceived when received`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                networkQueries.single().received(createChunk("Id"))
            } then {
                expect(sut.receivedDataItems has Size of 1)
            }

    @Test
    fun `query calls onTimeout if chunk is not received`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                networkQueries.single().timeout(Id("Id"))
            } then {
                expect(sut.receivedDataItems has Size of 0)
                expect(sut.timeoutCalls is_ Equal to_ 1)
            }

    @Test
    fun `query with subsequent queries signals success when all chunks are there`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                combinedChunks = null
                networkQueries[0].received(createChunk("dataId", "referencedId"))
                combinedChunks = byteArrayOf(1, 2, 3)
                networkQueries[1].received(createChunk("referencedId"))
            } then {
                expect(networkQueries.size is_ Equal to_ 2)
                expect(sut.receivedDataItems.single() is_ Equal to_ byteArrayOf(1, 2, 3))
            }

    @Test
    fun `query signals combination of all received chunks as result`() {
        lateinit var chunks: List<DataChunk>
        test when_ {
            chunks = listOf(
                    createChunk("dataId", "referencedId"),
                    createChunk("referencedId"))
            sut.protectedQuery(Id("Id"))
            combinedChunks = null
            networkQueries[0].received(chunks[0])
            combinedChunks = byteArrayOf(1, 2, 3)
            networkQueries[1].received(chunks[1])
        } then {
            inOrder(chunker) {
                verify(chunker).tryCombining(chunks.slice(0..0))
                verify(chunker).tryCombining(chunks.slice(0..1))
            }
            expect(sut.receivedDataItems.single() is_ Equal to_ byteArrayOf(1, 2, 3))
        }
    }

    @Test
    fun `already received chunk is not queried again`() =
            test when_ {
                val chunks = listOf(
                        createChunk("dataId", "referencedId"),
                        createChunk("referencedId", "dataId"))
                sut.protectedQuery(Id("Id"))
                networkQueries[0].received(chunks[0])
                networkQueries[1].received(chunks[1])
            } then {
                expect(networkQueries has Size of 2)
            }

    @Test
    fun `expired chunk can be retried`() =
            test when_ {
                sut.onTimeoutCallback = { sut.protectedTransportQueryControl.retry() }
                sut.protectedQuery(Id("Id"))
                networkQueries.single().timeout(Id("Id"))
            } then {
                expect(sut.timeoutCalls is_ Equal to_ 1)
                verify(networkQueries.single().queryControl).keepAlive()
            }

    @Test
    fun `if expired chunk is not retried, retrieving it later is not signaled`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                networkQueries.single().timeout(Id("Id"))
                networkQueries.single().received(createChunk("Id"))
            } then {
                expect(sut.timeoutCalls is_ Equal to_ 1)
                expect(sut.receivedDataItems is_ Empty)
            }

    @Test
    fun `if expired chunk is not retried, later timeout is not signaled`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                networkQueries.single().timeout(Id("Id"))
                networkQueries.single().timeout(Id("Id"))
            } then {
                expect(sut.timeoutCalls is_ Equal to_ 1)
            }

    @Test
    fun `if no pending chunks but chunker cannot combine, incomplete is signaled`() =
            test when_ {
                sut.protectedQuery(Id("Id"))
                combinedChunks = null
                networkQueries.single().received(createChunk("Id"))
            } then {
                expect(sut.incompleteCalls is_ Equal to_ 1)
            }

    @Test
    fun `transport query control number of retries is initially zero`() =
            test when_ {
                sut.protectedTransportQueryControl.numberOfRetries
            } then {
                expect(it.result is_ Equal to_ 0)
            }

    @Test
    fun `transport query control counts number of retries`() =
            test when_ {
                sut.protectedTransportQueryControl.retry()
            } then {
                expect(sut.protectedTransportQueryControl.numberOfRetries is_ Equal to_ 1)
            }

    @Test
    fun `receivedChunks contains all so far received chunks`() {
        lateinit var chunks: List<DataChunk>
        test when_ {
            chunks = listOf(
                    createChunk("dataId", "referencedId"),
                    createChunk("referencedId"))
            sut.protectedQuery(Id("Id"))
            networkQueries[0].received(chunks[0])
            networkQueries[1].received(chunks[1])
            sut.protectedReceivedChunks
        } then {
            expect(it.result is_ Equal to_ chunks)
        }
    }

    @Test
    fun `received chunk is marked as received`() {
        lateinit var chunk: DataChunk
        test when_ {
            chunk = createChunk("dataId")
            sut.protectedReceived(chunk) {}
        } then {
            expect(sut.protectedReceivedChunks.single() is_ Equal to_ chunk)
        }
    }

    @Test
    fun `received chunk trigger query of referenced chunks`() {
        lateinit var chunk: DataChunk
        test when_ {
            chunk = createChunk("dataId", "referencedId")
            sut.protectedReceived(chunk) {}
        } then {
            expect(networkQueries.single().query.chunkId is_ Equal to_ Id("referencedId"))
        }
    }

    @Test
    fun `received chunk triggers received callback if data is complete`() =
            test while_ {
                combinedChunks = byteArrayOf(1, 2, 3)
            } when_ {
                sut.protectedReceived(createChunk("dataId")) {}
            } then {
                expect(sut.receivedDataItems is_ Equal to_ Values(byteArrayOf(1, 2, 3)))
            }

    @Test
    fun `received chunk triggers keepAlive callback if data is incomplete and retry is called`() {
        var keepAliveCalled = false
        test while_ {
            combinedChunks = null
        } when_ {
            sut.onIncompleteCallback = { sut.protectedTransportQueryControl.retry() }
            sut.protectedReceived(createChunk("dataId")) { keepAliveCalled = true }
        } then {
            expect(keepAliveCalled)
        }
    }

    @Test
    fun `received chunk triggers not keepAlive callback if incomplete and retry is not called`() {
        var keepAliveCalled = false
        test while_ {
            combinedChunks = null
        } when_ {
            sut.protectedReceived(createChunk("dataId")) { keepAliveCalled = true }
        } then {
            expect(keepAliveCalled is_ False)
        }
    }

    @Test
    fun `further receiving of chunks is stopped on incomplete and retry is not called`() {
        test while_ {
            combinedChunks = null
        } when_ {
            sut.protectedReceived(createChunk("dataId")) {}
            combinedChunks = byteArrayOf(1, 2, 3)
            sut.protectedReceived(createChunk("dataId")) {}
        } then {
            expect(sut.receivedDataItems is_ Empty)
        }
    }

    @Test
    fun `further signaling of incomplete data is stopped on incomplete and retry is not called`() {
        test while_ {
            combinedChunks = null
        } when_ {
            sut.protectedReceived(createChunk("dataId")) {}
            sut.protectedReceived(createChunk("dataId")) {}
        } then {
            expect(sut.incompleteCalls is_ Equal to_ 1)
        }
    }

    @Test
    fun `further signaling of timeouts is stopped on incomplete and retry is not called`() {
        test while_ {
            combinedChunks = null
        } when_ {
            sut.protectedReceived(createChunk("dataId", "ref1")) {}
            sut.protectedReceived(createChunk("ref1")) {}
            networkQueries.single().timeout(Id("ref1"))
        } then {
            expect(sut.timeoutCalls is_ Equal to_ 0)
        }
    }
}
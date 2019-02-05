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
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.ChunkerCrypto
import straightway.peerspace.transport.DataQueryCallback
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.peerspace.transport.ListQuery
import straightway.peerspace.transport.ListQueryCallback
import straightway.peerspace.transport.transport
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.absolute
import straightway.units.get
import straightway.units.second
import java.time.Duration
import java.time.LocalDateTime

class TransportImplTest : KoinLoggingDisabler() {

    private val test get() =
        Given { TransportTestEnvironment(transportFactory = { TransportImpl() }) }

    companion object {
        val TransportTestEnvironment.sut get() = context.transport
    }

    @Test
    fun `store forwards small data directly to PeerClient`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(3, 2, 1)))
            } when_ {
                sut.store(byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                verify(peerClient).store(choppedChunks.single())
            }

    @Test
    fun `store returns id of small data chunk`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(3, 2, 1)))
            } when_ {
                sut.store(byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                expect(it.result is_ Equal to_ Id("0"))
            }

    @Test
    fun `store bigger data, all chunks are forwarded to PeerClient`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(1, 2)))
                choppedChunks.add(DataChunk(Key(Id("1")), byteArrayOf(3, 4)))
            } when_ {
                sut.store(byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                verify(peerClient).store(choppedChunks[0])
                verify(peerClient).store(choppedChunks[1])
            }

    @Test
    fun `store bigger data, first chunk is assumed directory chunk and id is returned`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(1, 2)))
                choppedChunks.add(DataChunk(Key(Id("1")), byteArrayOf(3, 4)))
            } when_ {
                sut.store(byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                expect(it.result is_ Equal to_ Id("0"))
            }

    @Test
    fun `store passes crypto to chunker`() {
        val crypto = ChunkerCrypto(signer = mock())
        test while_ {
            choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(3, 2, 1)))
        } when_ {
            sut.store(byteArrayOf(1, 2, 3), crypto)
        } then {
            verify(chunker).chopToChunks(any(), eq(crypto))
        }
    }

    @Test
    fun `post small chunk directly to peer client`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("hash")), byteArrayOf(1, 2)))
            } when_ {
                sut.post(Id("list"), byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                val timestamp =
                        Duration.between(LocalDateTime.of(0, 1, 1, 0, 0), currentTime).toMillis()
                verify(peerClient)
                        .store(DataChunk(Key(Id("list"), timestamp), byteArrayOf(1, 2)))
            }

    @Test
    fun `post bigger data, all chunks are forwarded to PeerClient`() =
            test while_ {
                choppedChunks.add(DataChunk(Key(Id("hash0")), byteArrayOf(1, 2)))
                choppedChunks.add(DataChunk(Key(Id("hash1")), byteArrayOf(3, 4)))
            } when_ {
                sut.post(Id("list"), byteArrayOf(1, 2, 3), ChunkerCrypto())
            } then {
                val timestamp =
                        Duration.between(LocalDateTime.of(0, 1, 1, 0, 0), currentTime).toMillis()
                verify(peerClient)
                        .store(DataChunk(Key(Id("list"), timestamp), byteArrayOf(1, 2)))
                verify(peerClient)
                        .store(choppedChunks.last())
            }

    @Test
    fun `post passes crypto to chunker`() {
        val crypto = ChunkerCrypto(signer = mock())
        test while_ {
            choppedChunks.add(DataChunk(Key(Id("0")), byteArrayOf(3, 2, 1)))
        } when_ {
            sut.post(Id("listId"), byteArrayOf(1, 2, 3), crypto)
        } then {
            verify(chunker).chopToChunks(any(), eq(crypto))
        }
    }

    @Test
    fun `query data item forwards execution to data query tracker`() {
        val setup: DataQueryCallback.() -> Unit = {}
        val passedCrypto = DeChunkerCrypto(signatureChecker = mock())
        lateinit var calledId: Id
        lateinit var calledSetup: DataQueryCallback.() -> Unit
        lateinit var calledCrypto: DeChunkerCrypto
        Given {
            TransportTestEnvironment(
                    transportFactory = { TransportImpl() },
                    dataQueryTrackerFactory = { id, crypto, setup ->
                        calledId = id
                        calledSetup = setup
                        calledCrypto = crypto
                        mock()
                    })
        } when_ {
            sut.query(Id("id"), passedCrypto, setup)
        } then {
            expect(calledId is_ Equal to_ Id("id"))
            expect(calledSetup is_ Same as_ setup)
            expect(calledCrypto is_ Same as_ passedCrypto)
        }
    }

    @Test
    fun `query list items forwards execution to list query tracker`() {
        val setup: ListQueryCallback.() -> Unit = {}
        val passedCrypto = DeChunkerCrypto(signatureChecker = mock())
        val query = ListQuery(Id("id"), 1[second].absolute..2[second].absolute)
        lateinit var calledQuery: ListQuery
        lateinit var calledSetup: ListQueryCallback.() -> Unit
        lateinit var calledCrypto: DeChunkerCrypto
        Given {
            TransportTestEnvironment(
                    transportFactory = { TransportImpl() },
                    listQueryTrackerFactory = { query, crypto, setup ->
                        calledQuery = query
                        calledSetup = setup
                        calledCrypto = crypto
                        mock()
                    })
        } when_ {
            sut.query(query, passedCrypto, setup)
        } then {
            expect(calledQuery is_ Same as_ query)
            expect(calledSetup is_ Same as_ setup)
            expect(calledCrypto is_ Same as_ passedCrypto)
        }
    }
}
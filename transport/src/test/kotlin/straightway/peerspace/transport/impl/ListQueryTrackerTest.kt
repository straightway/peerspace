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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.transport.ListDataItem
import straightway.peerspace.transport.ListDataKey
import straightway.peerspace.transport.ListQuery
import straightway.peerspace.transport.DataQueryControl
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.peerspace.transport.createListQueryTracker
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

class ListQueryTrackerTest : KoinLoggingDisabler() {

    private val test get() =
        Given {
            object {
                val environment = TransportTestEnvironment(
                        listQueryTrackerFactory = {
                            query, crypto, setup -> ListQueryTracker(query, crypto, setup)
                        },
                        listItemQueryTrackerFactory = { chunk, crypto, callbacks ->
                            listItemQueryTrackerInitialChunk = chunk
                            listItemQueryTrackerCallbacks = callbacks
                            listItemQueryTrackerCrypto = crypto
                            mock()
                        }
                )
                lateinit var listItemQueryTrackerInitialChunk: DataChunk
                lateinit var listItemQueryTrackerCallbacks: ListQueryCallbackInstances
                lateinit var listItemQueryTrackerCrypto: DeChunkerCrypto
            }
        }

    @Test
    fun `query is forwarded to peer client`() =
            test when_ {
                environment.context.createListQueryTracker(
                        ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                        DeChunkerCrypto(decryptor = mock())) {}
            } then {
                verify(environment.peerClient)
                        .query(eq(DataChunkQuery(Id("ListId"), 1000L..2000L)), any())
            }

    @Test
    fun `onExpired is called when initial query expires`() {
        var onExpiredCalled = false
        test when_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())
            ) {
                onExpired { onExpiredCalled = true }
            }
            environment.networkQueries[0].timeout(environment
                    .createChunk("ListId", 1.2[second]).key)
        } then {
            expect(onExpiredCalled)
        }
    }

    @Test
    fun `calling keepAlive onExpired keeps the query alive`() {
        test when_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())
            ) {
                onExpired { keepAlive() }
            }
            environment.networkQueries[0]
                    .timeout(environment.createChunk("ListId", 1.2[second]).key)
        } then {
            verify(environment.networkQueries[0].queryControl).keepAlive()
        }
    }

    @Test
    fun `not calling keepAlive onExpired does not keep the query alive`() {
        test when_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())) {}
            environment.networkQueries[0]
                    .timeout(environment.createChunk("ListId", 1.2[second]).key)
        } then {
            verify(environment.networkQueries[0].queryControl, never()).keepAlive()
        }
    }

    @Test
    fun `referenced chunks are queried via ListItemQueryTracker`() {
        lateinit var receivedChunk: DataChunk
        test when_ {
            receivedChunk = environment.createChunk("ListId", 1.2[second])
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())) {}
            environment.networkQueries[0].received(receivedChunk)
        } then {
            expect(listItemQueryTrackerInitialChunk is_ Equal to_ receivedChunk)
        }
    }

    @Test
    fun `received callback is forwarded to ListItemQueryTracker`() {
        var receivedCalled = false
        test while_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())
            ) {
                onReceived { receivedCalled = true }
            }
            environment.networkQueries[0].received(environment.createChunk("ListId", 1.2[second]))
        } when_ {
            listItemQueryTrackerCallbacks.onReceived(
                    ListDataItem(ListDataKey(Id("Id"), 1[second].absolute), byteArrayOf()))
        } then {
            expect(receivedCalled)
        }
    }

    @Test
    fun `onTimeout callback is forwarded to ListItemQueryTracker`() {
        var timeoutCalled = false
        test while_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())
            ) {
                onTimeout { timeoutCalled = true }
            }
            environment.networkQueries[0].received(environment.createChunk("ListId", 1.2[second]))
        } when_ {
            mock<DataQueryControl>().(listItemQueryTrackerCallbacks.onTimeout)(
                    ListDataKey(Id("Id"), 1[second].absolute))
        } then {
            expect(timeoutCalled)
        }
    }

    @Test
    fun `onIncomplete callback is forwarded to ListItemQueryTracker`() {
        var incompleteCalled = false
        test while_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    DeChunkerCrypto(decryptor = mock())
            ) {
                onIncomplete { _, _ -> incompleteCalled = true }
            }
            environment.networkQueries[0].received(environment.createChunk("ListId", 1.2[second]))
        } when_ {
            mock<DataQueryControl>().(listItemQueryTrackerCallbacks.onIncomplete)(
                    ListDataKey(Id("Id"), 1[second].absolute), listOf())
        } then {
            expect(incompleteCalled)
        }
    }

    @Test
    fun `crypto is passed to item tracker`() {
        val crypto = DeChunkerCrypto(signatureChecker = mock(), decryptor = mock())
        test when_ {
            environment.context.createListQueryTracker(
                    ListQuery(Id("ListId"), 1[second].absolute..2[second].absolute),
                    crypto) {}
            environment.networkQueries[0].received(environment.createChunk("ListId", 1.2[second]))
        } then {
            expect(listItemQueryTrackerCrypto is_ Same as_ crypto)
        }
    }
}
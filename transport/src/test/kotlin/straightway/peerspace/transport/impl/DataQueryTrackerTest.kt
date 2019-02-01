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

import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.transport.DataItem
import straightway.peerspace.transport.createDataQueryTracker
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataQueryTrackerTest : KoinLoggingDisabler() {

    private val test get() =
        Given {
            TransportTestEnvironment(
                    dataQueryTrackerFactory = { query, setup -> DataQueryTracker(query, setup) })
        }

    @Test
    fun `onReceived is called with id and complete data`() {
        lateinit var receivedData: DataItem
        val fullData = byteArrayOf(1, 2, 3)
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {
                onReceived { receivedData = it }
            }
            combinedChunks = fullData
            networkQueries.single().received(createChunk("chunkId"))
        } then {
            expect(receivedData is_ Equal to_ DataItem(Id("ItemId"), fullData))
        }
    }

    @Test
    fun `empty default onReceived callback does not throw`() {
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {}
            combinedChunks = byteArrayOf(1, 2, 3)
            networkQueries.single().received(createChunk("chunkId"))
        } then {
            expect({ it.result } does Not - Throw.exception)
        }
    }

    @Test
    fun `query calls onTimeout with queried data id`() {
        var timeoutId = Id("")
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {
                onTimeout { timeoutId = it }
            }
            networkQueries.single().timeout(Id("ChunkId"))
        } then {
            expect(timeoutId is_ Equal to_ Id("ItemId"))
        }
    }

    @Test
    fun `empty default onTimeout callback stops further receiving`() {
        var received = false
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {
                onReceived { received = true }
            }
            combinedChunks = byteArrayOf(1, 2, 3)
            networkQueries.single().timeout(Id("chunkId"))
            networkQueries.single().received(createChunk("chunkId"))
        } then {
            expect(received is_ False)
        }
    }

    @Test
    fun `query calls onIncomplete with queried data id`() {
        lateinit var incompleteId: Id
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {
                onIncomplete { id, _ -> incompleteId = id }
            }
            combinedChunks = null
            networkQueries.single().received(createChunk("ItemId"))
        } then {
            expect(incompleteId is_ Equal to_ Id("ItemId"))
        }
    }

    @Test
    fun `query calls onIncomplete with so far received chunks`() {
        lateinit var signaledChunks: List<DataChunk>
        lateinit var inputChunks: List<DataChunk>
        test when_ {
            inputChunks = listOf(createChunk("otherChunk"), createChunk("ItemId"))
            context.createDataQueryTracker(Id("ItemId")) {
                onIncomplete { _, chunks -> signaledChunks = chunks }
            }
            combinedChunks = null
            inputChunks.forEach { networkQueries.single().received(it) }
        } then {
            expect(signaledChunks.toSet() is_ Equal to_ inputChunks.toSet())
        }
    }

    @Test
    fun `empty default onIncomplete callback stops further receiving`() {
        var received = false
        test when_ {
            context.createDataQueryTracker(Id("ItemId")) {
                onReceived { received = true }
            }
            val chunk = createChunk("ItemId")
            combinedChunks = null
            networkQueries.single().received(chunk)
            combinedChunks = byteArrayOf(1, 2, 3)
            networkQueries.single().received(chunk)
        } then {
            expect(received is_ False)
        }
    }
}
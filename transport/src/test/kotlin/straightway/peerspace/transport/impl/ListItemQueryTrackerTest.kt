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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.transport.createListItemQueryTracker
import straightway.testing.bdd.Given
import straightway.testing.flow.expect
import straightway.units.get
import straightway.units.second

class ListItemQueryTrackerTest : KoinLoggingDisabler() {

    private companion object {
        val initialChunkWithoutReferences =
                DataChunk(Key(Id("Id"), 1200, 0), byteArrayOf())
    }

    private val test get() =
        Given {
            TransportTestEnvironment(
                    listItemQueryTrackerFactory = { init, cb ->
                        ListItemQueryTrackerImpl(init, cb)
                    })
        }

    @Test
    fun `initial chunk is marked as received`() {
        var isReceived = false
        test when_ {
            val callbacks = ListQueryCallbackInstances(
                    onReceived = { isReceived = true })
            combinedChunks = byteArrayOf(1, 2, 3)
            context.createListItemQueryTracker(initialChunkWithoutReferences, callbacks)
        } then {
            expect(isReceived)
        }
    }

    @Test
    fun `initial chunk without references does not trigger queries`() =
            test when_ {
                val callbacks = ListQueryCallbackInstances()
                combinedChunks = byteArrayOf(1, 2, 3)
                context.createListItemQueryTracker(initialChunkWithoutReferences, callbacks)
            } then {
                verify(peerClient, never()).query(any(), any())
            }

    @Test
    fun `referenced chunks are queried via peer client`() =
            test when_ {
                val callbacks = ListQueryCallbackInstances()
                val chunks = listOf(
                        createChunk("ListId", 1.2[second], "ReferencedId"),
                        createChunk("ReferencedId"))
                context.createListItemQueryTracker(chunks[0], callbacks)
            } then {
                verify(peerClient).query(eq(DataChunkQuery(Id("ReferencedId"))), any())
            }

    @Test
    fun `when referenced chunks timout, this is signaled`() {
        var isTimedOut = false
        test when_ {
            val callbacks = ListQueryCallbackInstances(onTimeout = { isTimedOut = true })
            val chunks = listOf(
                    createChunk("ListId", 1.2[second], "ReferencedId"),
                    createChunk("ReferencedId"))
            context.createListItemQueryTracker(chunks[0], callbacks)
            networkQueries.single().timeout(Id("ReferencedId"))
        } then {
            expect(isTimedOut)
        }
    }

    @Test
    fun `when data is incomplete, this is signaled`() {
        var isIncomplete = false
        test when_ {
            val callbacks = ListQueryCallbackInstances(onIncomplete = { _, _ ->
                isIncomplete = true
            })
            combinedChunks = null
            context.createListItemQueryTracker(initialChunkWithoutReferences, callbacks)
        } then {
            expect(isIncomplete)
        }
    }
}
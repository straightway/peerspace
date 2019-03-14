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
import com.nhaarman.mockito_kotlin.mock
import org.koin.dsl.context.Context
import straightway.peerspace.crypto.CryptoFactory
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.isUntimed
import straightway.peerspace.data.toTimestamp
import straightway.peerspace.net.PeerClient
import straightway.peerspace.net.ChunkQueryControl
import straightway.peerspace.transport.Chunker
import straightway.peerspace.transport.DataQueryCallback
import straightway.peerspace.transport.DeChunker
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.peerspace.transport.ListItemQueryTracker
import straightway.peerspace.transport.ListQuery
import straightway.peerspace.transport.ListQueryCallback
import straightway.peerspace.transport.Transport
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.chunker
import straightway.peerspace.transport.deChunker
import straightway.peerspace.transport.peerClient
import straightway.testing.flow.expect
import straightway.units.Time
import straightway.units.UnitValue
import straightway.units.absolute
import straightway.utils.TimeProvider
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray
import java.io.EOFException
import java.time.LocalDateTime

open class TransportTestEnvironment(
        transportFactory: TransportTestEnvironment.() -> Transport = { mock() },
        peerClientFactory: TransportTestEnvironment.() -> PeerClient = { createPeerClient() },
        chunkerFactory: TransportTestEnvironment.() -> Chunker = { createChunker() },
        deChunkerFactory: TransportTestEnvironment.() -> DeChunker = { createDeChunker() },
        timeProviderFactory: TransportTestEnvironment.() -> TimeProvider = { createTimeProvider() },
        listQueryTrackerFactory: TransportTestEnvironment.(
                ListQuery,
                crypto: DeChunkerCrypto,
                ListQueryCallback.() -> Unit) -> ListQueryCallback = { _, _, _ -> mock() },
        listItemQueryTrackerFactory: TransportTestEnvironment.(
                initialChunk: DataChunk,
                crypto: DeChunkerCrypto,
                callbacks: ListQueryCallbackInstances) -> ListItemQueryTracker =
                { _, _, _ -> mock() },
        dataQueryTrackerFactory: TransportTestEnvironment.(
                queriedId: Id,
                crypto: DeChunkerCrypto,
                querySetup: DataQueryCallback.() -> Unit) -> DataQueryCallback =
                { _, _, _ -> mock() },
        cryptoFactory: TransportTestEnvironment.() -> CryptoFactory = { mock() },
        additionalInitialization: Context.() -> Unit = {}
) {

    data class QueryRecord(val query: DataChunkQuery,
                           val receiveCallback: ChunkQueryControl.(DataChunk) -> Unit,
                           val queryControl: ChunkQueryControl,
                           val timeout: (Key) -> Unit
    ) {
        companion object {
            @Suppress("LongParameterList")
            operator fun invoke(query: DataChunkQuery,
                                receiveCallback: ChunkQueryControl.(DataChunk) -> Unit,
                                queryControl: ChunkQueryControl,
                                timeout: (Id) -> Unit
            ) = QueryRecord(query, receiveCallback, queryControl) { key: Key ->
                expect(key.isUntimed)
                timeout(key.id)
            }
        }
        fun received(data: DataChunk) = queryControl.receiveCallback(data)
        fun timeout(id: Id) = timeout(Key(id))
        override fun toString() = query.toString()
    }

    val currentTime =
            LocalDateTime.of(2023, 2, 3, 23, 13)!!
    val choppedChunks = mutableListOf<DataChunk>()
    var combinedChunks: ByteArray? = byteArrayOf()
    val networkQueries = mutableListOf<QueryRecord>()
    val peerClient get() = context.peerClient
    val chunker get() = context.chunker
    val deChunker get() = context.deChunker
    val context = TransportComponent.createEnvironment(
            transportFactory = { transportFactory() },
            peerClientFactory = { peerClientFactory() },
            chunkerFactory = { chunkerFactory() },
            deChunkerFactory = { deChunkerFactory() },
            cryptoFactory = { cryptoFactory() },
            timeProviderFactory = { timeProviderFactory() },
            listQueryTrackerFactory = { listQuery, crypto, setup ->
                listQueryTrackerFactory(listQuery, crypto, setup) },
            listItemQueryTrackerFactory = { initialChunk, crypto, callbacks ->
                listItemQueryTrackerFactory(initialChunk, crypto, callbacks) },
            dataQueryTrackerFactory = { queriedId, crypto, setup ->
                dataQueryTrackerFactory(queriedId, crypto, setup) }
    ) {
        additionalInitialization()
    }

    fun createChunk(id: String, vararg ids: String) =
            DataChunk(Key(Id(id)), ids.map { it }.serializeToByteArray())

    @Suppress("LongParameterList")
    fun createChunk(id: String, timestamp: LocalDateTime, vararg ids: String) =
            DataChunk(
                    Key(Id(id), timestamp.toTimestamp()),
                    ids.map { it }.serializeToByteArray())

    @Suppress("LongParameterList")
    fun createChunk(id: String, timestamp: UnitValue<Time>, vararg ids: String) =
            createChunk(id, timestamp.absolute, *ids)

    @Suppress("SwallowedException")
    fun ByteArray.getReferences() = try {
        deserializeTo<List<String>>().map { Id(it) }
    } catch (ex: EOFException) {
        listOf<Id>()
    }

    private fun createTimeProvider() = mock<TimeProvider> {
        on { now }.thenAnswer { currentTime }
    }

    private fun createChunker() = mock<Chunker> {
        on { chopToChunks(any(), any()) }.thenAnswer { choppedChunks.toSet() }
    }

    private fun createDeChunker() = mock<DeChunker> {
        on { getReferencedChunks(any(), any()) }
                .thenAnswer { it.getArgument<ByteArray>(0).getReferences() }
        on { tryCombining(any(), any()) }
                .thenAnswer { combinedChunks }
    }

    @Suppress("OptionalUnit")
    private fun createPeerClient() = mock<PeerClient> {
        on { query(any(), any()) }.thenAnswer { args ->
            var onExpiringCallback: ChunkQueryControl.(DataChunkQuery) -> Unit = {}
            mock<ChunkQueryControl> {
                on { onExpiring(any()) }.thenAnswer { args ->
                    onExpiringCallback = args.getArgument(0)
                    Unit
                }
            }.also { queryControl ->
                val query: DataChunkQuery = args.getArgument(0)
                networkQueries.add(QueryRecord(query, args.getArgument(1), queryControl) {
                    queryControl.onExpiringCallback(query)
                })
            }
        }
    }
}
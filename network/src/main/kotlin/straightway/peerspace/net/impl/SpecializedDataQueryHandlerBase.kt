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

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Bean.inject
import straightway.koinutils.Property.property
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.isMatching
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.Network
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.Transmission
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.getPendingQueriesForChunk
import straightway.peerspace.net.isPending

/**
 * Base class for DataQueryHandler implementations.
 */
abstract class SpecializedDataQueryHandlerBase(
        val isLocalResultPreventingForwarding: Boolean) :
        DataQueryHandler,
        KoinModuleComponent by KoinModuleComponent() {

    private val peerId: Id by property("peerId") { Id(it) }
    private val network: Network by inject()
    private val dataChunkStore: DataChunkStore by inject()
    private val forwardTracker: ForwardStateTracker<DataQueryRequest>
            by inject("queryForwardTracker")

    final override fun handle(query: DataQueryRequest) {
        if (!pendingDataQueryTracker.isPending(query)) handleNewQueryRequest(query)
    }

    final override fun getForwardPeerIdsFor(chunkKey: Key) =
            pendingDataQueryTracker.getPendingQueriesForChunk(chunkKey)
                    .filter { !chunkKey.isAlreadyForwardedFor(it) }
                    .map { it.query.originatorId }

    final override fun notifyChunkForwarded(key: Key) {
        val matchingChunks = dataChunkStore.query(DataQueryRequest(peerId, DataQuery(key)))
        val matchingQueries = pendingDataQueryTracker.pendingDataQueries.filter {
            it.query.query.isMatching(key)
        }
        matchingQueries.forEach { matchingChunks forwardTo it.query.originatorId }
        onChunkForwarding(key)
    }

    protected open fun onChunkForwardFailed(chunkKey: Key, targetId: Id) {}

    protected abstract fun onChunkForwarding(key: Key)

    protected abstract val pendingDataQueryTracker: PendingDataQueryTracker

    protected abstract fun splitToEpochs(request: DataQueryRequest): Iterable<DataQueryRequest>

    private fun Key.isAlreadyForwardedFor(it: PendingDataQuery) =
            it.forwardedChunkKeys.contains(this)

    private val DataQueryRequest.result get() = dataChunkStore.query(this)

    private fun handleNewQueryRequest(query: DataQueryRequest) {
        pendingDataQueryTracker.setPending(query)
        val hasLocalResult = returnLocalResult(query)
        if (hasLocalResult && isLocalResultPreventingForwarding) return
        forward(query)
    }

    private fun forward(query: DataQueryRequest) =
            splitToEpochs(query).forEach {
                forwardTracker.forward(it)
            }

    private fun returnLocalResult(query: DataQueryRequest): Boolean {
        val localResult = query.result.toList()
        localResult forwardTo query.originatorId
        return localResult.any()
    }

    private infix fun Iterable<DataChunk>.forwardTo(targetId: Id) =
        forEach { chunk -> chunk forwardTo targetId }

    private infix fun DataChunk.forwardTo(targetId: Id) =
            network.scheduleTransmission(
                    Transmission(targetId, DataPushRequest(peerId, this)),
                    object : TransmissionResultListener {
                        override fun notifySuccess() {}
                        override fun notifyFailure() = onChunkForwardFailed(key, targetId)
                    })
}

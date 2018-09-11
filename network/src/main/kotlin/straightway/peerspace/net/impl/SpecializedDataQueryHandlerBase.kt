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
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.isMatching
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.PendingDataQuery
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.Request
import straightway.peerspace.net.TransmissionResultListener
import straightway.peerspace.net.dataChunkStore
import straightway.peerspace.net.getPendingQueriesForChunk
import straightway.peerspace.net.isPending
import straightway.peerspace.net.network
import straightway.peerspace.net.queryForwarder

/**
 * Base class for DataQueryHandler implementations.
 */
abstract class SpecializedDataQueryHandlerBase(
        val isLocalResultPreventingForwarding: Boolean) :
        DataQueryHandler,
        KoinModuleComponent by KoinModuleComponent() {

    final override fun handle(query: Request<DataQuery>) {
        if (!pendingDataQueryTracker.isPending(query.content))
            handleNewQueryRequest(query)
    }

    final override fun getForwardPeerIdsFor(chunkKey: Key) =
            pendingDataQueryTracker.getPendingQueriesForChunk(chunkKey)
                    .filter { !chunkKey.isAlreadyForwardedFor(it) }
                    .map { it.query.remotePeerId }

    final override fun notifyChunkForwarded(key: Key) {
        val matchingChunks = dataChunkStore.query(DataQuery(key.id, key.timestamps))
        val matchingQueries = pendingDataQueryTracker.pendingDataQueries.filter {
            it.query.content.isMatching(key)
        }
        matchingQueries.forEach { matchingChunks forwardTo it.query.remotePeerId }
        onChunkForwarding(key)
    }

    protected open fun onChunkForwardFailed(chunkKey: Key, targetId: Id) {}

    protected abstract fun onChunkForwarding(key: Key)

    protected abstract val pendingDataQueryTracker: PendingDataQueryTracker

    protected abstract fun splitToEpochs(query: DataQuery): Iterable<DataQuery>

    private fun Key.isAlreadyForwardedFor(it: PendingDataQuery) =
            it.forwardedChunkKeys.contains(this)

    private val DataQuery.result get() = dataChunkStore.query(this)

    private fun handleNewQueryRequest(query: Request<DataQuery>) {
        pendingDataQueryTracker.setPending(query)
        val hasLocalResult = returnLocalResult(query)
        if (hasLocalResult && isLocalResultPreventingForwarding) return
        forward(query)
    }

    private fun forward(request: Request<DataQuery>) =
            splitToEpochs(request.content).forEach {
                queryForwarder.forward(Request(request.remotePeerId, it))
            }

    private fun returnLocalResult(query: Request<DataQuery>): Boolean {
        val localResult = query.content.result.toList()
        localResult forwardTo query.remotePeerId
        return localResult.any()
    }

    private infix fun Iterable<DataChunk>.forwardTo(targetId: Id) =
        forEach { chunk -> chunk forwardTo targetId }

    private infix fun DataChunk.forwardTo(targetId: Id) =
            network.scheduleTransmission(
                    Request(targetId, this),
                    object : TransmissionResultListener {
                        override fun notifySuccess() {}
                        override fun notifyFailure() = onChunkForwardFailed(key, targetId)
                    })
}

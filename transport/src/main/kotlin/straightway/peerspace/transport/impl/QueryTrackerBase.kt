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

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.ChunkQueryControl as ChunkQueryControl
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.chunker
import straightway.peerspace.transport.peerClient
import straightway.peerspace.transport.DataQueryControl as DataQueryControl

/**
 * Base class for data or list query trackers.
 */
abstract class QueryTrackerBase : TransportComponent by TransportComponent() {

    private var pendingChunks = mutableMapOf<Key, DataChunk?>()
    private var isStopped = false

    protected abstract fun onReceived(data: ByteArray)
    protected abstract fun onIncomplete()
    protected abstract fun onTimeout()

    protected val transportQueryControl: DataQueryControl = DataQueryControlImpl()

    protected val receivedChunks get() =
        pendingChunks.values.filter { it !== null }.map { it!! }

    protected fun query(chunkId: Id) {
        setPending(Key(chunkId))
        peerClient.query(DataChunkQuery(chunkId)) { received(it, this::keepAlive) }
                .onExpiring { onChunkTimedOut() }
    }

    protected fun received(chunk: DataChunk, keepAlive: () -> Unit) {
        if (isStopped) return
        setReceived(chunk)
        queryChunksReferencedBy(chunk)
        signalResultDataIfAvailable(keepAlive)
    }

    private fun setReceived(chunk: DataChunk) { pendingChunks[chunk.key] = chunk }

    private fun setPending(chunkKey: Key) { pendingChunks[chunkKey] = null }

    private fun signalResultDataIfAvailable(keepAlive: () -> Unit) =
            with (chunker.tryCombining(receivedChunks)) {
                when {
                    this != null -> onReceived(this)
                    areNoChunksPending -> checkForRetry(keepAlive) { onIncomplete() }
                }
            }

    private val areNoChunksPending get() = pendingChunks.values.all { it !== null }

    private fun queryChunksReferencedBy(chunk: DataChunk) =
            chunk.newReferences.forEach { query(it) }

    private val DataChunk.newReferences get() = references.filter { Key(it) !in pendingChunks }
    private val DataChunk.references get() = chunker.getReferencedChunks(data)

    private fun ChunkQueryControl.onChunkTimedOut() =
            checkForRetry(this::keepAlive) { onTimeout() }

    private fun checkForRetry(
            keepAlive: () -> Unit,
            action: DataQueryControl.() -> Unit
    ) {
        if (isStopped) return
        val oldNumberOfRetries = transportQueryControl.numberOfRetries
        transportQueryControl.action()
        if (oldNumberOfRetries < transportQueryControl.numberOfRetries)
            keepAlive()
        else isStopped = true
    }

    private class DataQueryControlImpl : DataQueryControl {
        private var _numberOfRetries = 0
        override val numberOfRetries get() = _numberOfRetries
        override fun retry() { ++_numberOfRetries }
    }

}
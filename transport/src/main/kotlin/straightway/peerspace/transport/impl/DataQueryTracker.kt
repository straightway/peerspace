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
import straightway.peerspace.data.Id
import straightway.peerspace.transport.DataItem
import straightway.peerspace.transport.DataQueryCallback
import straightway.peerspace.transport.DataQueryControl

/**
 * Track queries for static data in peerspace.
 */
class DataQueryTracker(
        private val queriedId: Id,
        querySetup: DataQueryCallback.() -> Unit
) : QueryTrackerBase(), DataQueryCallback {

    private var onReceived: (DataItem) -> Unit = {}
    private var onTimeout: DataQueryControl.(Id) -> Unit = {}
    private var onIncomplete: DataQueryControl.(Id, List<DataChunk>) -> Unit = { _, _ -> }

    init {
        querySetup()
        query(queriedId)
    }

    override fun onReceived(data: ByteArray) = onReceived(DataItem(queriedId, data))
    override fun onTimeout() = transportQueryControl.onTimeout(queriedId)
    override fun onIncomplete() = transportQueryControl.onIncomplete(queriedId, receivedChunks)

    override fun onReceived(callback: (DataItem) -> Unit) {
        onReceived = callback
    }

    override fun onTimeout(callback: DataQueryControl.(Id) -> Unit) {
        onTimeout = callback
    }

    override fun onIncomplete(callback: DataQueryControl.(Id, List<DataChunk>) -> Unit) {
        onIncomplete = callback
    }
}
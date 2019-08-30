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
import straightway.peerspace.transport.DeChunkerCrypto
import straightway.peerspace.transport.ListDataItem
import straightway.peerspace.transport.ListItemQueryTracker
import straightway.peerspace.transport.ListQueryCallbackInstances
import straightway.peerspace.transport.toListDataKey

/**
 * Track receiving of all data chunks for a single peerspace list item.
 */
class ListItemQueryTrackerImpl(
        val initialChunk: DataChunk,
        crypto: DeChunkerCrypto,
        val callbacks: ListQueryCallbackInstances
) : QueryTrackerBase(crypto), ListItemQueryTracker {

    init {
        received(initialChunk, this::onTimeout)
    }

    override fun onReceived(data: ByteArray) =
            callbacks.onReceived(ListDataItem(listItemKey, data))
    override fun onTimeout() =
            transportQueryControl.(callbacks.onTimeout)(listItemKey)
    override fun onIncomplete() =
            transportQueryControl.(callbacks.onIncomplete)(listItemKey, receivedChunks)

    private val listItemKey get() = initialChunk.key.toListDataKey()
}

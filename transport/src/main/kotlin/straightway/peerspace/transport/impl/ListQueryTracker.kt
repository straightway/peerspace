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

import straightway.koinutils.Bean.inject
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.toTimestamp
import straightway.peerspace.net.PeerClient
import straightway.peerspace.transport.ListDataItem
import straightway.peerspace.transport.ListDataKey
import straightway.peerspace.transport.ListQuery
import straightway.peerspace.transport.ListQueryCallback
import straightway.peerspace.transport.DataQueryControl
import straightway.peerspace.transport.ListQueryControl
import straightway.peerspace.transport.TransportComponent
import straightway.peerspace.transport.createListItemQueryTracker

/**
 * Track querying the chunks for all items in a peerspace list satisfying the query.
 */
class ListQueryTracker(
        query: ListQuery,
        querySetup: ListQueryCallback.() -> Unit
) : ListQueryCallback, TransportComponent by TransportComponent() {

    private val peerClient: PeerClient by inject()

    private var listQueryCallbackInstances = ListQueryCallbackInstances()

    private var onExpired: ListQueryControl.() -> Unit = {}

    init {
        querySetup()
        peerClient.query(DataChunkQuery(query.listId, query.timestamps.toTimestamp())) {
            createListItemQueryTracker(it, listQueryCallbackInstances)
        }.onExpiring {
            object : ListQueryControl {
                override fun keepAlive() { this@onExpiring.keepAlive() }
            }.onExpired()
        }
    }

    override fun onReceived(callback: (ListDataItem) -> Unit) {
        listQueryCallbackInstances = listQueryCallbackInstances.copy(onReceived = callback)
    }

    override fun onTimeout(callback: DataQueryControl.(ListDataKey) -> Unit) {
        listQueryCallbackInstances = listQueryCallbackInstances.copy(onTimeout = callback)
    }

    override fun onIncomplete(callback: DataQueryControl.(ListDataKey, List<DataChunk>) -> Unit) {
        listQueryCallbackInstances = listQueryCallbackInstances.copy(onIncomplete = callback)
    }

    override fun onExpired(callback: ListQueryControl.() -> Unit) {
        onExpired = callback
    }
}
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

import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.PendingQueryTracker
import straightway.peerspace.net.getPendingQueriesForChunk

/**
 * DataQueryHandler for timed queries.
 */
class TimedDataQueryHandler :
        SpecializedDataQueryHandlerBase(isLocalResultPreventingForwarding = false),
        DataQueryHandler {

    override val pendingQueryTracker: PendingQueryTracker
            by inject("pendingTimedQueryTracker")

    override fun notifyChunkForwarded(key: Key) =
            pendingQueryTracker.getPendingQueriesForChunk(key).forEach {
                pendingQueryTracker.addForwardedChunk(it, key)
            }
}

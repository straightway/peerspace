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
package straightway.peerspace.integrationtest

import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Identifyable
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget

/**
 * A simulated client of the peerspace network.
 */
class NetworkClient(override val id: Id) : Identifyable, DataPushTarget {

    override fun push(request: DataPushRequest) {
        _receivedData += request.chunk
    }

    val receivedData: List<DataChunk> get() = _receivedData
    private val _receivedData = mutableListOf<DataChunk>()
}
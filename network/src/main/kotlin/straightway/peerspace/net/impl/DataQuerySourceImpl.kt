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

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.TransmissionResultListener

/**
 * Default implementation of the DataQuerySource interface.
 */
class DataQuerySourceImpl : DataQuerySource, KoinModuleComponent by KoinModuleComponent() {

    private val peerDirectory: PeerDirectory by inject()
    private val dataQueryHandler: DataQueryHandler by inject("dataQueryHandler")
    private val network: Network by inject()

    override fun query(
            request: DataQueryRequest,
            resultListener: TransmissionResultListener
    ) {
        peerDirectory.add(request.originatorId)
        dataQueryHandler.handle(request)
        network.executePendingRequests()
        resultListener.notifySuccess()
    }
}
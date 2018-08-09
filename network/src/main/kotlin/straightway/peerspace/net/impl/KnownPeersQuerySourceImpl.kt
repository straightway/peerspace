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
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.TransmissionResultListener

/**
 * Default implementation of the KnownPeersQuerySource interface.
 */
class KnownPeersQuerySourceImpl :
        KnownPeersQuerySource,
        KoinModuleComponent by KoinModuleComponent() {

    private val knownPeersProvider: KnownPeersProvider by inject()

    override fun query(
            request: KnownPeersQueryRequest,
            resultListener: TransmissionResultListener
    ) {
        knownPeersProvider.pushKnownPeersTo(request.originatorId)
        resultListener.notifySuccess()
    }
}
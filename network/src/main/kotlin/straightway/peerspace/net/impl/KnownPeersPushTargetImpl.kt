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
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.Request

/**
 * Default implementation of the KnownPeersPushTarget interface.
 */
class KnownPeersPushTargetImpl :
        KnownPeersPushTarget,
        KoinModuleComponent by KoinModuleComponent() {

    private val peerDirectory: PeerDirectory by inject()

    override fun pushKnownPeers(request: Request<KnownPeers>) {
        peerDirectory.add(request.remotePeerId)
        request.content.knownPeersIds.forEach { peerDirectory.add(it) }
    }
}
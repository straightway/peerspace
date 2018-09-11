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

import straightway.peerspace.data.Id
import straightway.koinutils.Bean.init
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.net.Peer
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.dataPushTarget
import straightway.peerspace.net.dataQuerySource
import straightway.peerspace.net.knownPeersGetter
import straightway.peerspace.net.knownPeersPushTarget
import straightway.peerspace.net.knownPeersQuerySource

@Suppress("MoveLambdaOutsideParentheses")
/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl :
        Peer,
        DataPushTarget by init({ dataPushTarget }),
        DataQuerySource by init({ dataQuerySource }),
        KnownPeersPushTarget by init({ knownPeersPushTarget }),
        KnownPeersQuerySource by init({ knownPeersQuerySource }),
        KnownPeersGetter by init({ knownPeersGetter }),
        KoinModuleComponent by KoinModuleComponent() {

    override val id: Id by property("peerId") { Id(it) }

    override fun toString() = "PeerImpl(${id.identifier})"
}
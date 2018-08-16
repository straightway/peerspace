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
import straightway.koinutils.Bean
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.net.Peer
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.RequestHandler
import straightway.peerspace.net.Transmittable
import straightway.utils.getHandlers

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl :
        Peer,
        DataPushTarget by Bean.get("localDataPushTarget"),
        DataQuerySource by Bean.get("localDataQuerySource"),
        KnownPeersPushTarget by Bean.get("localKnownPeersPushTarget"),
        KnownPeersQuerySource by Bean.get("localKnownPeersQuerySource"),
        KnownPeersGetter by Bean.get(),
        KoinModuleComponent by KoinModuleComponent() {

    override val id: Id by property("peerId") { Id(it) }

    override fun toString() = "PeerImpl(${id.identifier})"
}

fun Peer.handle(request: Transmittable) =
        getHandlers<RequestHandler>(request::class).forEach { it(request) }
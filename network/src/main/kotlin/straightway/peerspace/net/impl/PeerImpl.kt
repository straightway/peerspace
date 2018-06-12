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
@file:Suppress("ForbiddenComment")
package straightway.peerspace.net.impl

import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.koinutils.Property.property
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.random.Chooser

// TODO:
// * Avoid routing loops:
// ** Don't push the same chunk twice to the same peer (within a certain time)
// * Collect known peers while receiving requests

/**
 * Default productive implementation of a peerspace peer.
 */
class PeerImpl : Peer, KoinModuleComponent by KoinModuleComponent() {

    override val id: Id by property("peerId") { Id(it) }

    fun refreshKnownPeers() =
        peersToQueryForOtherKnownPeers.forEach { queryForKnownPeers(it) }

    override fun push(request: PushRequest, resultListener: TransmissionResultListener) {
        dataChunkStore.store(request.chunk)
        dataPushForwarder.forward(request)
        resultListener.notifySuccess()
    }

    override fun query(request: QueryRequest, resultListener: TransmissionResultListener) {
        when (request.id) {
            Administrative.KnownPeers.id ->
                knownPeersProvider.pushKnownPeersTo(request.originatorId)
            else ->
                dataQueryHandler.handle(request)
        }
        resultListener.notifySuccess()
    }

    override fun toString() = "PeerImpl(${id.identifier})"

    private val configuration: Configuration by inject()
    private val dataChunkStore: DataChunkStore by inject()
    private val dataQueryHandler: DataQueryHandler by inject()
    private val network: Network by inject()
    private val knownPeersProvider: KnownPeersProvider by inject()
    private val dataPushForwarder: DataPushForwarder by inject()
    private val peerDirectory: PeerDirectory by inject()
    private val knownPeerQueryChooser: Chooser by inject("knownPeerQueryChooser")

    private val peersToQueryForOtherKnownPeers
        get() = knownPeerQueryChooser choosePeers configuration.maxPeersToQueryForKnownPeers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(peerDirectory.allKnownPeersIds.toList(), number)

    private fun queryForKnownPeers(peerId: Id) =
        peerId.asQuerySource.query(QueryRequest(id, Administrative.KnownPeers))

    private val Id.asQuerySource
        get() = network.getQuerySource(this)
}
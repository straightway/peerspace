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
package straightway.peerspace.networksimulator

import straightway.peerspace.data.Id
import straightway.koinutils.Bean.inject
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.Channel
import straightway.peerspace.net.PeerComponent
import straightway.peerspace.net.Request
import straightway.peerspace.net.chunkSizeGetter
import straightway.peerspace.net.handle
import straightway.peerspace.net.localPeerId
import straightway.peerspace.net.peer
import straightway.sim.net.Message
import straightway.sim.net.Node
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream

/**
 * Infrastructure for the simulation of a network peer.
 */
class SimNode : Node, PeerComponent by PeerComponent() {

    private val simNodes: MutableMap<Id, SimNode> by inject("simNodes")
    private val transmissionRequestHandler: TransmissionRequestHandler by inject()

    override val id: Id get() = localPeerId
    override val uploadStream: TransmissionStream by inject("uploadStream")
    override val downloadStream: TransmissionStream by inject("downloadStream")

    override var isOnline: Boolean
        get() = uploadStream.isOnline && downloadStream.isOnline
        set(isOnline) {
            uploadStream.isOnline = isOnline
            downloadStream.isOnline = isOnline
        }

    override fun notifyReceive(sender: Node, message: Message) {
        val messageContent = message.content
        if (messageContent is Transmittable)
            peer handle Request.createDynamically(
                    sender.id as? Id ?: Id("<invalid>"), messageContent)
    }

    override fun notifySuccess(receiver: Node) {}

    override fun notifyFailure(receiver: Node) {}

    fun createChannel(id: Id): Channel =
            SimChannel(
                    transmissionRequestHandler,
                    chunkSizeGetter,
                    from = this,
                    to = simNodes[id]!!)

    init {
        simNodes[peer.id] = this
    }
}